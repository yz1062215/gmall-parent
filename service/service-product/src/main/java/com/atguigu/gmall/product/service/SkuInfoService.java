package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【sku_info(库存单元表)】的数据库操作Service
 * @createDate 2022-08-23 13:45:28
 */
public interface SkuInfoService extends IService<SkuInfo> {


    void saveSkuInfo(SkuInfo info);

    void cancelSale(Long skuId);

    void onSale(Long skuId);

    SkuDetailTo getSkuDetail(Long skuId);

    /**
     * 获取sku实时价格
     *
     * @return
     */
    BigDecimal getSku101Price(Long skuId);


    SkuInfo getDetailSkuInfo(Long skuId);

    List<SkuImage> getDetailSkuImages(Long skuId);

    List<Long> findAllSkuId();

    /**
     * 修改商品属性 延时双删缓存
     *
     * @param skuInfo
     */
    void updateSkuInfo(SkuInfo skuInfo);

    /**
     * 得到某个skuId在es中存储的所有数据
     *
     * @param skuId
     * @return
     */
    Goods getGoodsBySkuId(Long skuId);
}
