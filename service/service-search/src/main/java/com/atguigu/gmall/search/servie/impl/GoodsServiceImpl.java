package com.atguigu.gmall.search.servie.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.vo.search.*;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.servie.GoodsService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsRepository goodsRepository;
    @Override
    public void saveGoods(Goods goods) {
        goodsRepository.save(goods);
    }

    @Override
    public void del(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * ES中检索商品
     * @param paramVo
     * @return
     */
    @Autowired
    ElasticsearchRestTemplate esRestTemplate;  //es复杂查询客户端
    @Override
    public SearchResponseVo search(SearchParamVo paramVo) {
        //1.构造动态搜索条件
        Query query = buildQueryDsl(paramVo);

        //2.搜索业务
        SearchHits<Goods> goods = esRestTemplate.search(query,
                Goods.class,
                IndexCoordinates.of("goods"));

        //3.搜索结果转换
        SearchResponseVo searchResponseVo=buildSearchResponseResult(goods,paramVo);

        return searchResponseVo;
    }

    @Override
    public void updateScore(Long skuId, Long score) {
        //找到对应的商品
        Goods goods = goodsRepository.findById(skuId).get();

        //更新得分
        goods.setHotScore(score);
        //同步到es
        goodsRepository.save(goods);
    }

    /**
     * 构建es查询的响应结果
     * @param goods
     * @return
     */
    private SearchResponseVo buildSearchResponseResult(SearchHits<Goods> goods,SearchParamVo paramVo) {
        SearchResponseVo vo = new SearchResponseVo();

        //检索时前端响应检索条件
        vo.setSearchParam(paramVo);
        //构建品牌面包屑   1:小米
        if (!StringUtils.isEmpty(paramVo.getTrademark())){
            vo.setTrademarkParam("品牌"+paramVo.getTrademark().split(":")[1]); //品牌：小米
        }
        //构建平台属性面包屑
        if (paramVo.getProps()!=null&&paramVo.getProps().length > 0){
            List<SearchAttr> propsParams=new ArrayList<>();
            //24:8G：运行内存
            for (String prop : paramVo.getProps()) {
                String[] split = prop.split(":");

                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(Long.valueOf(split[0]));
                searchAttr.setAttrValue(split[1]);
                searchAttr.setAttrName(split[2]);

                propsParams.add(searchAttr);

            }
            vo.setPropsParamList(propsParams);
        }

        //品牌列表  聚合分析 TODO
        List<TrademarkVo> trademarkVos=buildTrademarkList(goods);
        vo.setTrademarkList(trademarkVos);
        //属性列表 聚合分析 TODO
        List<AttrVo> attrVos=buildAttrList(goods);
        vo.setAttrsList(attrVos);

        //排序信息   回显  1:desc
        if (!StringUtils.isEmpty(paramVo.getOrder())){
            String order = paramVo.getOrder();
            OrderMapVo mapVo = new OrderMapVo();
            mapVo.setType(order.split(":")[0]);
            mapVo.setSort(order.split(":")[1]);
            vo.setOrderMap(mapVo);
        }

        //所有搜索到的商品列表
        List<Goods> goodsList = new ArrayList<>();
        List<SearchHit<Goods>> hits = goods.getSearchHits();//获取搜索到的商品
        for (SearchHit<Goods> hit : hits) {
            Goods content = hit.getContent();//命中记录的商品
            //如果模糊检索了  处理高亮
            if (!StringUtils.isEmpty(paramVo.getKeyword())){
                String title = hit.getHighlightField("title").get(0);//拿到第一个高亮关键字
                //设置高亮
                content.setTitle(title);
            }
            goodsList.add(content);
        }
        vo.setGoodsList(goodsList);
        //页码
        vo.setPageNo(paramVo.getPageNo());

        //总页码
        long totalHits = goods.getTotalHits();
        //获取命中的页数 对命中条数数据取余，余数为0返回整数除结果，余数不为,页码+1
        long pageSize = totalHits % SysRedisConst.SEARCH_PAGESIZE == 0 ?
                totalHits / SysRedisConst.SEARCH_PAGESIZE
                : (totalHits / SysRedisConst.SEARCH_PAGESIZE + 1);
        vo.setTotalPages(new Integer(pageSize+""));

        //旧链接地址
        String url=makeUrlParam(paramVo);
        vo.setUrlParam(url);
        return vo;
    }

    /**
     * 分析检索结果中有多少平台属性
     * @param goods
     * @return
     */
    private List<AttrVo> buildAttrList(SearchHits<Goods> goods) {

        List<AttrVo> attrVos = new ArrayList<>();
        //获取attrAgg属性聚合
        ParsedNested attrAgg=goods.getAggregations().get("attrAgg");

        //获取属性id的聚合结果
        ParsedLongTerms attrIdAgg=attrAgg.getAggregations().get("attrIdAgg");
        //遍历所有的属性id
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            AttrVo attrVo = new AttrVo();

            //获取属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //获取属性名
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            // 获取属性值
            List<String> attrValues=new ArrayList<>();
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            for (Terms.Bucket valueAggBucket : attrValueAgg.getBuckets()) {
                String value = valueAggBucket.getKeyAsString();
                attrValues.add(value);
            }
            attrVo.setAttrValueList(attrValues);
            attrVos.add(attrVo);
        }

        return attrVos;
    }

    /**
     * 分析当前检索结果中包含多少品牌
     * @param goods
     * @return
     */
    private List<TrademarkVo> buildTrademarkList(SearchHits<Goods> goods) {
        List<TrademarkVo> trademarkVos=new ArrayList<>();
        //拿到 tmIdAgg 聚合
        ParsedLongTerms tmIdAgg=goods.getAggregations().get("tmIdAgg");

        //获取品牌ID桶内聚合的每一个数据
        for (Terms.Bucket bucket : tmIdAgg.getBuckets()) {
            TrademarkVo trademarkVo = new TrademarkVo();

            //获取品牌Id
            Long tmId = bucket.getKeyAsNumber().longValue();
            trademarkVo.setTmId(tmId);

            //获取品牌名
            ParsedStringTerms tmNameAgg=bucket.getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            trademarkVo.setTmName(tmName);
            //获取品牌logo
            ParsedStringTerms tmLogoAgg= bucket.getAggregations().get("tmLogoAgg");
            String tmLogoUrl = tmLogoAgg.getBuckets().get(0).getKeyAsString();
            trademarkVo.setTmLogoUrl(tmLogoUrl);
            trademarkVos.add(trademarkVo);
        }
        return trademarkVos;
    }

    /**
     * 获取旧链接
     * @param paramVo
     * @return
     */
    private String makeUrlParam(SearchParamVo paramVo) {
        //构建String
        StringBuilder builder = new StringBuilder("list.html?");
        //1.分类参数
        if (paramVo.getCategory1Id()!=null){
            builder.append("&category1Id="+paramVo.getCategory1Id());
        }
        if (paramVo.getCategory2Id()!=null){
            builder.append("&category2Id="+paramVo.getCategory2Id());
        }
        if (paramVo.getCategory3Id()!=null){
            builder.append("&category3Id="+paramVo.getCategory3Id());

        }
        //2.关键字
        if (!StringUtils.isEmpty(paramVo.getKeyword())) {
            builder.append("&keyword=" + paramVo.getKeyword());
        }
        //3.品牌
        if (!StringUtils.isEmpty(paramVo.getTrademark())){
            builder.append("&trademark="+paramVo.getTrademark());
        }
        //4.属性
        if (paramVo.getProps() != null && paramVo.getProps().length > 0) {
            String[] props = paramVo.getProps();
            for (String prop : props) {
                builder.append("&props=" + prop);
            }
        }
        ////5.排序
        //builder.append("&order=" + paramVo.getOrder());
        ////6.页码
        //builder.append("&pageNo"+paramVo.getPageNo());

        return builder.toString();

    }

    /**
     * 根据前端传来的请求参数构建ES搜索条件
     * @param paramVo
     * @return
     */
    private Query buildQueryDsl(SearchParamVo paramVo) {
        
        //构建Bool查询条件
        //准备bool查询（多条件查询）
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //给bool中添加must

        //传入分类条件
        if (paramVo.getCategory1Id()!=null){
            //如果1级分类不为空
            boolQuery.must(QueryBuilders.termQuery("category1Id",paramVo.getCategory1Id()));
        }
        if (paramVo.getCategory2Id()!=null){
            //如果2级分类不为空
            boolQuery.must(QueryBuilders.termQuery("category2Id",paramVo.getCategory2Id()));
        }
        if (paramVo.getCategory3Id()!=null){
            //如果2级分类不为空
            boolQuery.must(QueryBuilders.termQuery("category3Id",paramVo.getCategory3Id()));
        }

        //传入keywords  直接进行全文检索
        if (!StringUtils.isEmpty(paramVo.getKeyword())){
           //进行math检索   即分词匹配
            boolQuery.must(QueryBuilders.matchQuery("title", paramVo.getKeyword()));
        }

        //传入品牌信息 trademark=4:小米
        if (!StringUtils.isEmpty(paramVo.getTrademark())){
            long tmId = Long.parseLong(paramVo.getTrademark().split(":")[0]);//截取前端传来的品牌id
            boolQuery.must(QueryBuilders.termQuery("tmId", tmId));
        }

        //传入属性
        String[] props = paramVo.getProps();
        if(props!=null&&props.length > 0){
            for (String prop : props) {
                //4:128:机身存储  得到属性和值
                String[] split = prop.split(":");
                Long attrId = Long.parseLong(split[0]);
                String attrValue=split[1];

                //构造boolQuery
                BoolQueryBuilder nestedBool = QueryBuilders.boolQuery();
                //第一个must
                nestedBool.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                //第二个must
                nestedBool.must(QueryBuilders.termQuery("attrs.attrValue",attrValue));

                NestedQueryBuilder nestedQuery = QueryBuilders.
                        nestedQuery("attrs", nestedBool, ScoreMode.None);

                //给最大的bool放入nested查询
                boolQuery.must(nestedQuery);
            }
        }


        //检索条件结束..............


        //准备原生检索条件
        NativeSearchQuery query = new NativeSearchQuery(boolQuery);

        //传入排序
        if (!StringUtils.isEmpty(paramVo.getOrder())) {
            //2:asc
            String[] split = paramVo.getOrder().split(":");
            //String prop = split[0].equals("1") ? "hotScore" : "price";

            //判断排序使用的字段为什么
            String orderField="hotScore";
            switch (split[0]) {
                case "1": orderField = "hotScore";break;
                case "2": orderField = "price";break;
                case "3": orderField = "createTime";break;
                default: orderField = "hotScore";

            }
            Sort sort = Sort.by(orderField);
            if (split[1].equals("asc")) {
                sort = sort.ascending();//会返回sort
            } else {
                sort = sort.descending();
            }
            query.addSort(sort);
        }

        //传入页码
        //if (paramVo.getPageNo()!=null &&paramVo.getPageNo()>0){

            //spring底层页码从0开始  所以要给前端传来的数据-1
            PageRequest pageRequest = PageRequest.of(paramVo.getPageNo()-1, SysRedisConst.SEARCH_PAGESIZE);
            query.setPageable(pageRequest);

        //}
        //排序分页结束................


        //高亮检索
        if (!StringUtils.isEmpty(paramVo.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title").preTags("<span style='color:red'>").postTags("</span>");
            HighlightQuery highlightQuery = new HighlightQuery(highlightBuilder);
            query.setHighlightQuery(highlightQuery);
        }
        //模糊查询高亮结束..............


        //TODO  聚合分析...

        //3、品牌聚合 - 品牌聚合分析条件
        TermsAggregationBuilder tmIdAgg = AggregationBuilders
                .terms("tmIdAgg")
                .field("tmId")
                .size(1000);


        //品牌聚合分析
        //子聚合   品牌名
        TermsAggregationBuilder tmName = AggregationBuilders.terms("tmNameAgg").field("tmName").size(1);
        //子聚合   品牌logo
        TermsAggregationBuilder tmLogo = AggregationBuilders.terms("tmLogoAgg").field("tmLogoUrl").size(1);

        tmIdAgg.subAggregation(tmName);
        tmIdAgg.subAggregation(tmLogo);

        query.addAggregation(tmIdAgg);

        //平台属性聚合
        //构建嵌套环境
        NestedAggregationBuilder nested = AggregationBuilders.nested("attrAgg", "attrs");
        //attrId聚合
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(100);
        //attrName聚合
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1);
        //attrValue聚合
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(100);
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);
        nested.subAggregation(attrIdAgg);
        query.addAggregation(nested);


        return query;
    }
}
