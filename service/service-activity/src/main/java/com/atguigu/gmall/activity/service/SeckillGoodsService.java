package com.atguigu.gmall.activity.service;


import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author yzz
 * @description 针对表【seckill_goods】的数据库操作Service
 * @createDate 2022-08-26 09:14:06
 */
public interface SeckillGoodsService extends IService<SeckillGoods> {
    /**
     * 获取当天参与秒杀的商品列表
     *
     * @return
     */
    List<SeckillGoods> getCurrentDaySeckillGoodsList();

    /**
     * 本地缓存中查询寻秒杀商品列表
     *
     * @return
     */
    List<SeckillGoods> getCurrentDaySeckillCache();

    /**
     * 减少秒杀库存
     *
     * @param skuId
     */
    void deduceSeckillGoods(Long skuId);
}
