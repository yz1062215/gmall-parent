package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.web.feign.SkuDetailFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
    商品详情
 */
@Controller
public class ItemController {
    @Autowired
    SkuDetailFeignClient skuDetailFeignClient;

    /**
     * 查看商品详情
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {

        //远程调用查询商品详细信息
        Result<SkuDetailTo> result = skuDetailFeignClient.getSkuDetail(skuId);
        //1.商品(sku)所属的完整分类
        //2.商品(sku)的基本信息
        //3.sku图片
        //4.sku所属的spu的所有销售属性值   标识出当前sku到底是spu下的哪种组合  对该种组合高亮提示

        //5.商品sku的类似推荐
        //6.商品介绍 规格参数 售后评论....
        //判断是否调用成功
        if (result.isOk()) {
            //调用成功
            SkuDetailTo skuDetailTo = result.getData();
            if (skuDetailTo==null||skuDetailTo.getSkuInfo()==null){
                //说明远程未查到商品
                return "item/404";
            }
            model.addAttribute("categoryView", skuDetailTo.getCategoryView());
            model.addAttribute("skuInfo", skuDetailTo.getSkuInfo());
            model.addAttribute("price", skuDetailTo.getPrice());
            model.addAttribute("spuSaleAttrList", skuDetailTo.getSpuSaleAttrList());//spu的销售属性列表
            model.addAttribute("valuesSkuJson", skuDetailTo.getValuesSkuJson());//json

        }
        return "item/index";
    }
}
