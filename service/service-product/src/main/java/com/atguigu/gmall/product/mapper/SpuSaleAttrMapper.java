package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yzz
* @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Mapper
* @createDate 2022-08-23 13:45:28
* @Entity com.atguigu.gmall.product.domain.SpuSaleAttr
*/
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    /**
     * 根据spuId查询所有的商品属性名和属性值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getspuSaleAttrListBySpuId(@Param("spuId") Long spuId);

    List<SpuSaleAttr> getSaleAttrAndValueAndMarkSkuBySpuId(@Param("spuId") Long spuId, @Param("skuId") Long skuId);
}




