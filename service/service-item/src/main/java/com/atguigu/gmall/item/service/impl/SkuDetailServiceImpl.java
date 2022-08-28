package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    SkuDetailFeignClient skuDetailFeignClient;
    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {
        SkuDetailTo detailTo = new SkuDetailTo();
        //远程调用service-product服务
        //Result<SkuDetailTo> skuDetail = skuDetailFeignClient.getSkuDetail(skuId);

        //v2  拆分粒度
        //1.查询基本信息
        Result<SkuInfo> result = skuDetailFeignClient.getSkuInfo(skuId);
        SkuInfo skuInfo= result.getData();
        //TODO  全局异常处理
        detailTo.setSkuInfo(skuInfo);
        //2.查询商品图片信息
        Result<List<SkuImage>> skuImages = skuDetailFeignClient.getSkuImages(skuId);
        skuInfo.setSkuImageList(skuImages.getData());
        //3.查询商品实时价格
        Result<BigDecimal> sku101Price = skuDetailFeignClient.getSku101Price(skuId);
        detailTo.setPrice(sku101Price.getData());
        //4.查询销售属性名和值
        Result<List<SpuSaleAttr>> attrValues = skuDetailFeignClient.getsaleAttrValues(skuId, skuInfo.getSpuId());
        detailTo.setSpuSaleAttrList(attrValues.getData());
        //5.查询sku组合
        Result<String> skuValueJson = skuDetailFeignClient.getSkuValueJson(skuInfo.getSpuId());
        detailTo.setValuesSkuJson(skuValueJson.getData());
        //6.查询分类
        Result<CategoryViewTo> viewToResult = skuDetailFeignClient.getcategory(skuInfo.getCategory3Id());
        detailTo.setCategoryView(viewToResult.getData());


        //1.商品(sku)所属的完整分类
        //2.商品(sku)的基本信息
        //3.sku图片
        //4.sku所属的spu的所有销售属性值   标识出当前sku到底是spu下的哪种组合  对该种组合高亮提示
        //5.商品sku的类似推荐
        //6.商品介绍 规格参数 售后评论....
        return detailTo;
    }
}
