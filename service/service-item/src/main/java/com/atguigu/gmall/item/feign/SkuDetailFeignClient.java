package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/api/inner/rpc/product")
@FeignClient("service-product")
public interface SkuDetailFeignClient {

    //v1
    //@GetMapping("/skudetail/{skuId}")
    //public Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId);

    /**
     * 查询sku的基本信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/info/{skuId}")
    Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 查询sku的详细分类
     *
     * @param c3Id
     * @return
     */
    @GetMapping("/skudetail/categoryview/{c3Id}")
    Result<CategoryViewTo> getcategory(@PathVariable("c3Id") Long c3Id);

    /**
     * 查询sku所有图片
     *
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/images/{skuId}")
    Result<List<SkuImage>> getSkuImages(@PathVariable("skuId") Long skuId);

    /**
     * 获取sku的实时价格
     *
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/price/{skuId}")
    Result<BigDecimal> getSku101Price(@PathVariable("skuId") Long skuId);

    /**
     * 查询sku对应的spu定义的所有销售属性名和值 并标记
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/saleAttrValues/{skuId}/{spuId}")
    Result<List<SpuSaleAttr>> getsaleAttrValues(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId);

    /**
     * 查询sku所有组合
     *
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/valuejson/{spuId}")
    Result<String> getSkuValueJson(@PathVariable("spuId") Long spuId);
}
