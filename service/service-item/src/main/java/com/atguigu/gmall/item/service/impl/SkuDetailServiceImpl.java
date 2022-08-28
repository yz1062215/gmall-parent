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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    SkuDetailFeignClient skuDetailFeignClient;
    @Autowired
    ThreadPoolExecutor executor;
    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {
        SkuDetailTo detailTo = new SkuDetailTo();
        CountDownLatch downLatch = new CountDownLatch(6);//异步编排
        //远程调用service-product服务
        //Result<SkuDetailTo> skuDetail = skuDetailFeignClient.getSkuDetail(skuId);
        //CompletableFuture.runAsync(); 不用返回结果
        //CompletableFuture.supplyAsync(); 需要返回结果
        //v2  拆分粒度
        //1.查询基本信息
        //异步编排处理
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            Result<SkuInfo> result = skuDetailFeignClient.getSkuInfo(skuId);
            SkuInfo skuInfo = result.getData();
            //TODO  全局异常处理
            detailTo.setSkuInfo(skuInfo);
            return skuInfo;
        },executor);//返回skuInfo

        //2.查询商品图片信息
        CompletableFuture<Void> imageFuture = skuInfoFuture.thenAcceptAsync((skuInfo) -> {
            Result<List<SkuImage>> skuImages = skuDetailFeignClient.getSkuImages(skuId);
            skuInfo.setSkuImageList(skuImages.getData());
        }, executor);//需要接收放回结果

        //3.查询商品实时价格   不需要前面返回结果
        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            Result<BigDecimal> sku101Price = skuDetailFeignClient.getSku101Price(skuId);
            detailTo.setPrice(sku101Price.getData());
        }, executor);

        //4.查询销售属性名和值   需要前面返回结果
        CompletableFuture<Void> attrValuesFuture = skuInfoFuture.thenAcceptAsync((skuInfo) -> {
            Result<List<SpuSaleAttr>> attrValues = skuDetailFeignClient.getsaleAttrValues(skuId, skuInfo.getSpuId());
            detailTo.setSpuSaleAttrList(attrValues.getData());
        }, executor);

        //5.查询sku组合
        CompletableFuture<Void> skuValueJsonFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Result<String> skuValueJson = skuDetailFeignClient.getSkuValueJson(skuInfo.getSpuId());
            detailTo.setValuesSkuJson(skuValueJson.getData());
        }, executor);

        //6.查询分类
        CompletableFuture<Void> catagoryFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Result<CategoryViewTo> viewToResult = skuDetailFeignClient.getcategory(skuInfo.getCategory3Id());
            detailTo.setCategoryView(viewToResult.getData());
        }, executor);


        //全部线程执行完后
        CompletableFuture.allOf(imageFuture,priceFuture,attrValuesFuture,skuValueJsonFuture,catagoryFuture).join();


        //1.商品(sku)所属的完整分类
        //2.商品(sku)的基本信息
        //3.sku图片
        //4.sku所属的spu的所有销售属性值   标识出当前sku到底是spu下的哪种组合  对该种组合高亮提示
        //5.商品sku的类似推荐
        //6.商品介绍 规格参数 售后评论....
        return detailTo;
    }
}
