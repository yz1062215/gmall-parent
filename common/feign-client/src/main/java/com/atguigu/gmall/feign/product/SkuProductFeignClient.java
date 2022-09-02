package com.atguigu.gmall.feign.product;

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
public interface SkuProductFeignClient {

//    @GetMapping("/skudetail/{skuId}")  //不要用这个超级接口
//    Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId);

    /**
     * 查询sku的基本信息
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/info/{skuId}")
    Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId);


    /**
     * 查询sku的图片信息
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/images/{skuId}")
    Result<List<SkuImage>> getSkuImages(@PathVariable("skuId")Long skuId);


    /**
     * 查询sku的实时价格
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/price/{skuId}")
    Result<BigDecimal> getSku101Price(@PathVariable("skuId")Long skuId);

    /**
     * 查询sku对应的spu定义的所有销售属性名和值。并且标记出当前sku是哪个
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/saleAttrValues/{skuId}/{spuId}")
    public Result<List<SpuSaleAttr>> getsaleAttrValues(@PathVariable("skuId") Long skuId,
                                                       @PathVariable("spuId") Long spuId);


    /**
     * 查sku组合 valueJson
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/valuejson/{spuId}")
    public Result<String> getSkuValueJson(@PathVariable("spuId")Long spuId);


    /**
     * 查分类
     * @param c3Id
     * @return
     */
    @GetMapping("/skudetail/categoryview/{c3Id}")
    public Result<CategoryViewTo> getcategory(@PathVariable("c3Id") Long c3Id);

}
