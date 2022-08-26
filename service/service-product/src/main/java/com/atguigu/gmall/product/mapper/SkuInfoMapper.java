package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
* @author yzz
* @description 针对表【sku_info(库存单元表)】的数据库操作Mapper
* @createDate 2022-08-23 13:45:28
* @Entity com.atguigu.gmall.product.domain.SkuInfo
*/
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {
    /**
     * 更新sku的sale属性
     * @param skuId
     * @param sale
     */
    void updateIsSale(@Param("skuId") Long skuId, @Param("sale") int sale);

    /**
     * 获取商品实时价格
     * @param skuId
     * @return
     */
    BigDecimal get101Price(@Param("skuId") Long skuId);



}




