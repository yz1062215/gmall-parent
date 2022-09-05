package com.atguigu.gmall.search.servie.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.model.vo.search.SearchResponseVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.servie.GoodsService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        IndexCoordinates goods = IndexCoordinates.of("goods");
        SearchHits<Goods> searchHits = esRestTemplate.search(query,
                Goods.class,
                goods);

        //3.搜索结果转换

        return null;
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


        //准备原生检索条件
        NativeSearchQuery query = new NativeSearchQuery(boolQuery);

        //传入排序
        if (!StringUtils.isEmpty(paramVo.getOrder())) {
            //2:asc
            String[] split = paramVo.getOrder().split(":");
            //String prop = split[0].equals("1") ? "hotScore" : "price";

            //判断排序使用的字段为什么
            String orderFiled="hotScore";
            switch (split[0]) {
                case "1":
                    orderFiled = "hotScore";
                    break;
                case "2":
                    orderFiled = "price";
                    break;
                case "3":
                    orderFiled = "createTime";
                default:
                    orderFiled = "hotScore";

            }
            Sort sort = Sort.by(orderFiled);
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



        return query;
    }
}
