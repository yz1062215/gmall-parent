package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yzz
* @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Service
* @createDate 2022-08-23 13:45:28
*/
public interface SpuSaleAttrService extends IService<SpuSaleAttr> {
    /**
     * 根据spuId查询所有销售属性名和值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    /**
     * 根据spuId skuId查询所有属性名和值 并标记出来
     * @param spuId
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> getSaleAttrAndValueAndMarkSkuBySpuId(Long spuId, Long skuId);



}
