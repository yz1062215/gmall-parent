package com.atguigu.gmall.product.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.product.service.BaseCategory3Service;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情数据库层操作
 */
@RestController
@RequestMapping("/api/inner/rpc/product")
public class SkuDetailApiController {
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SpuSaleAttrService spuSaleAttrService;
    @Autowired
    BaseCategory3Service baseCategory3Service;


    //v1
    //@GetMapping("/skudetail/{skuId}")
    //public Result<SkuDetailTo> getSkuDetail(@PathVariable("skuId") Long skuId) {
    //    SkuDetailTo skuDetailTo = skuInfoService.getSkuDetail(skuId);
    //    return Result.ok(skuDetailTo);
    //}

    /**
     * 查询sku的基本信息
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/info/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable("skuId") Long skuId) {
        SkuInfo skuInfo=skuInfoService.getDetailSkuInfo(skuId);
        return Result.ok(skuInfo);
    }

    /**
     * 查询sku的详细分类
     * @param c3Id
     * @return
     */
    @GetMapping("/skudetail/categoryview/{c3Id}")
    public Result<CategoryViewTo> getcategory(@PathVariable("c3Id") Long c3Id) {
        CategoryViewTo categoryViewTo=baseCategory3Service.getCategoryView(c3Id);
        return Result.ok(categoryViewTo);
    }


    /**
     * 查询sku所有图片
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/images/{skuId}")
    public Result<List<SkuImage>> getSkuImages(@PathVariable("skuId") Long skuId){
        List<SkuImage> skuImages=skuInfoService.getDetailSkuImages(skuId);
        return Result.ok(skuImages);
    }

    /**
     * 获取sku的实时价格
     * @param skuId
     * @return
     */
    @GetMapping("/skudetail/price/{skuId}")
    public Result<BigDecimal> getSku101Price(@PathVariable("skuId") Long skuId){
        BigDecimal price = skuInfoService.getSku101Price(skuId);
        return Result.ok(price);
    }

    /**
     * 查询sku对应的spu定义的所有销售属性名和值 并标记
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/saleAttrValues/{skuId}/{spuId}")
    public Result<List<SpuSaleAttr>> getsaleAttrValues(@PathVariable("skuId") Long skuId,
                                    @PathVariable("spuId") Long spuId){
        List<SpuSaleAttr> saleAttrList=spuSaleAttrService.getSaleAttrAndValueAndMarkSkuBySpuId(spuId,skuId);
        return Result.ok(saleAttrList);
    }

    /**
     * 查询sku所有组合
     * @param spuId
     * @return
     */
    @GetMapping("/skudetail/valuejson/{spuId}")
    public Result<String> getSkuValueJson(@PathVariable("spuId")Long spuId){
        String valueJson =spuSaleAttrService.getAllSkuSaleValueJson(spuId);
        return Result.ok(valueJson);
    }
}
