package com.atguigu.gmall.activity.mapper;


import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yzz
 * @description 针对表【seckill_goods】的数据库操作Mapper
 * @createDate 2022-08-26 09:14:06
 * @Entity com.atguigu.gmall.activity.domain.SeckillGoods
 */
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    List<SeckillGoods> getSeckillGoodsByDate(@Param("date") String date);

    void updateStockCount(@Param("skuId") Long skuId);
}




