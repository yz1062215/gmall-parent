package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.feign.search.SearchFeignClient;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.*;
import com.atguigu.starter.cache.annotation.GmallCache;
import com.atguigu.starter.cache.service.CacheOpsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【sku_info(库存单元表)】的数据库操作Service实现
 * @createDate 2022-08-23 13:45:28
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {

    @Autowired
    SkuImageService skuImageService;

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    RedissonClient redissonClient;

    @Resource
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SpuSaleAttrService spuSaleAttrService;


    /**
     * sku大保存
     *
     * @param info
     */
    @Transactional//开启事务
    @Override
    public void saveSkuInfo(SkuInfo info) {
        //1、sku基本信息保存到 sku_info
        save(info);
        Long skuId = info.getId();//回填使用的id
        //2、sku的图片信息保存到 sku_image
        for (SkuImage skuImage : info.getSkuImageList()) {
            skuImage.setSkuId(skuId);
        }
        skuImageService.saveBatch(info.getSkuImageList());

        //3、sku的平台属性名和值的关系保存到 sku_attr_value
        List<SkuAttrValue> attrValueList = info.getSkuAttrValueList();
        for (SkuAttrValue attrValue : attrValueList) {
            attrValue.setSkuId(skuId);
        }
        skuAttrValueService.saveBatch(attrValueList);

        //4、sku的销售属性名和值的关系保存到 sku_sale_attr_value
        List<SkuSaleAttrValue> saleAttrValueList = info.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : saleAttrValueList) {
            saleAttrValue.setSkuId(skuId);
            saleAttrValue.setSpuId(info.getSpuId());
        }
        skuSaleAttrValueService.saveBatch(saleAttrValueList);

        //TODO  新增一个商品 将这个商品的skuID放到布隆过滤器
        RBloomFilter<Object> filter = redissonClient.getBloomFilter(SysRedisConst.BLOOM_SKUID);
        filter.add(skuId);

    }
    //下架

    /**
     * 下架
     *
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        //修改sku_info表中的is_sale
        skuInfoMapper.updateIsSale(skuId, 0);

        //TODO es中删除该数据
        searchFeignClient.delGoods(skuId);
    }
    //上架
    @Autowired
    SearchFeignClient searchFeignClient;

    /**
     * 上架
     *
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        skuInfoMapper.updateIsSale(skuId, 1);

        //TODO es中新增该数据
        Goods goods = getGoodsBySkuId(skuId);
        searchFeignClient.saveGoods(goods);

    }


    /**
     * 获取sku商品详情....
     *
     * @param skuId
     * @return
     */
    @Resource
    BaseCategory3Mapper baseCategory3Mapper;


    @Deprecated//过期方法
    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {

        //TODO √  1.商品(sku)所属的完整分类
        SkuDetailTo detailTo = new SkuDetailTo();
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);//获取商品详情
        //1.1根据三级分类id获取完整分类信息并封装到CategoryViewTo 中
        CategoryViewTo categoryViewTo = baseCategory3Mapper.getCategoryView(skuInfo.getCategory3Id());
        detailTo.setCategoryView(categoryViewTo);

        //TODO √  2.商品(sku)的基本信息
        detailTo.setSkuInfo(skuInfo);
        //TODO √   3.sku图片
        List<SkuImage> imageList = skuImageService.getSkuImage(skuId);
        skuInfo.setSkuImageList(imageList);

        //实时价格查询
        detailTo.setPrice(getSku101Price(skuId));
        //4.sku所属的spu的所有销售属性值   标识出当前sku到底是spu下的哪种组合  对该种组合高亮提示
        //根据spuId和skuId查询
        List<SpuSaleAttr> saleAttrList = spuSaleAttrService.getSaleAttrAndValueAndMarkSkuBySpuId(skuInfo.getSpuId(), skuId);
        detailTo.setSpuSaleAttrList(saleAttrList);

        //获取sku的所有兄弟产品的销售名和值组合关系
        /*
        SELECT a.sku_id,
            GROUP_CONCAT(DISTINCT sale_attr_value_id ORDER BY sale_attr_value_id SEPARATOR '|')
            AS value_json
            FROM
            (SELECT si.`id` sku_id,
            skuav.`spu_id`,
            skuav.`sale_attr_value_id`,
            spuav.`base_sale_attr_id`
             FROM sku_info si
            LEFT JOIN sku_sale_attr_value skuav ON si.`id`=skuav.`sku_id`
            LEFT JOIN spu_sale_attr_value spuav ON skuav.`sale_attr_value_id`=spuav.`id`
            WHERE si.`spu_id`=27 ORDER BY si.`id`,spuav.`base_sale_attr_id`,skuav.`sale_attr_value_id`) a
            GROUP BY a.sku_id
         */
        //传入spu_id
        Long spuId = skuInfo.getSpuId();
        String valueJson = spuSaleAttrService.getAllSkuSaleValueJson(spuId);
        detailTo.setValuesSkuJson(valueJson);


        //5.商品sku的类似推荐
        //6.商品介绍 规格参数 售后评论....
        return detailTo;
    }

    @Override
    public BigDecimal getSku101Price(Long skuId) {
        BigDecimal price = skuInfoMapper.get101Price(skuId);
        return price;
    }

    /**
     * //1.查询skuinfo
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getDetailSkuInfo(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);//获取商品详情
        return skuInfo;
    }

    /**
     * 2.查询sku商品图片集合
     *
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getDetailSkuImages(Long skuId) {
        List<SkuImage> imageList = skuImageService.getSkuImage(skuId);
        return imageList;
    }

    @Override
    public List<Long> findAllSkuId() {

        //查询出所有id
        List<Long> ids = skuInfoMapper.getAllSkuId();
        return ids;
    }


    @Autowired
    CacheOpsService cacheOpsService;

    @GmallCache
    @Override
    public void updateSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.update(skuInfo, null);
        cacheOpsService.delay2Delete(skuInfo.getId().toString());
    }

    @Autowired
    BaseTrademarkService baseTrademarkService;

    @Override
    public Goods getGoodsBySkuId(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        Goods goods = new Goods();

        goods.setId(skuId);

        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setCreateTime(new Date());
        goods.setTmId(skuInfo.getTmId());

        BaseTrademark trademark = baseTrademarkService.getById(skuInfo.getTmId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());

        //根据skuId查询分类详情
        Long category3Id = skuInfo.getCategory3Id();
        CategoryViewTo categoryView = baseCategory3Mapper.getCategoryView(category3Id);
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        // TODO 热度分更新
        goods.setHotScore(0L);
        //查询当前sku所有平台属性和值
        List<SearchAttr> attrs = skuAttrValueService.getAttrNameAndVal(skuId);
        goods.setAttrs(attrs);


        return goods;
    }
}




