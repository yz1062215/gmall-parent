package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yzz
* @description 针对表【spu_info(商品表)】的数据库操作Service
* @createDate 2022-08-23 13:45:28
*/
public interface SpuInfoService extends IService<SpuInfo> {

    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    List<BaseSaleAttr> getBaseSaleAttrList();

    List<SpuImage> getSpuImageList(Long spuId);

    void saveSpuInfo(SpuInfo spuInfo);
}
