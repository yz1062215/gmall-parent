package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    销售属性api
 */
@RestController
@RequestMapping("/admin/product")
public class SpuManageController {
    @Autowired
    SpuInfoService spuInfoService;

    //http://192.168.6.1/admin/product/1/10?category3Id=2

    /**
     *分页查询SPU属性
     * @param pageNum
     * @param pageSize
     * @param spuInfo
     * @return
     */
    @GetMapping("/{pageNum}/{pageSize}")
    public Result getSpuInfoPage(@PathVariable("pageNum")Long pageNum,
                                 @PathVariable("pageSize")Long pageSize,
                                 SpuInfo spuInfo){
        Page<SpuInfo> spuInfoPage = new Page<>(pageNum,pageSize);
        IPage<SpuInfo> spuInfoPageList=spuInfoService.getSpuInfoPage(spuInfoPage,spuInfo);
        return Result.ok(spuInfoPageList);
    }
    //http://192.168.6.1/admin/product/baseSaleAttrList

    /**
     * 查询spu销售属性集合
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList= spuInfoService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    /**
     * 保存spu
     * @param spuInfo
     * @return
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuInfoService.save(spuInfo);
        return Result.ok();
    }

    /**
     * 查询spu属性得到图片集合
     * @param spuId
     * @return
     */
    //http://192.168.6.1/admin/product/spuImageList/28
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId")Long spuId){
        List<SpuImage> spuImageList=spuInfoService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }
    //http://192.168.6.1/admin/product/spuSaleAttrList/27
}
