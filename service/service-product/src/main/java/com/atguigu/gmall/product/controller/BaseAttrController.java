package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.service.BaseAttrValueService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    商品属性api
 */
@Api(tags = "商品基础属性接口")
@RestController
@RequestMapping("/admin/product")
public class BaseAttrController {
    @Autowired
    BaseAttrInfoService baseAttrInfoService;
    @Autowired
    BaseAttrValueService baseAttrValueService;
    @Autowired
    SpuSaleAttrService spuSaleAttrService;

    //http://192.168.6.1/admin/product/attrInfoList/2/0/0

    /**
     * 查询某个分类下的所有平台属性
     * @return
     */
    @GetMapping("/attrInfoList/{c1Id}/{c2Id}/{c3Id}")
    public Result getAttrInfoList(@PathVariable("c1Id") Long c1Id,
                                  @PathVariable("c2Id") Long c2Id,
                                  @PathVariable("c3Id") Long c3Id) {
        List<BaseAttrInfo> infos = baseAttrInfoService.getAttrInfoAndValueByCategoryId(c1Id, c2Id, c3Id);
        return Result.ok(infos);
    }
    //http://192.168.6.1/admin/product/saveAttrInfo

    /**
     * 保存平台属性  修改平台属性
     * @param info
     * @return
     */
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo info){
        baseAttrInfoService.saveAttrInfo(info);
        return Result.ok();
    }
    //http://192.168.6.1/admin/product/getAttrValueList/11

    /**
     * 根据平台属性ID获取平台属性对象数据
     * @param attrId
     * @return
     */
    @GetMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable("attrId")Long attrId){
        List<BaseAttrValue> values=baseAttrValueService.getAttrValueList(attrId);
        return Result.ok(values);
    }

    //http://192.168.6.1/admin/product/spuSaleAttrList/27

    /**
     *查询指定spu当时定义的所有销售属性的名和值
     * @param spuId
     * @return
     */
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable("spuId")Long spuId){
        List<SpuSaleAttr> spuSaleAttrs=spuSaleAttrService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrs);
    }
}
