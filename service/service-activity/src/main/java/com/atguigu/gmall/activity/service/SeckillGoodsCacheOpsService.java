package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsCacheOpsService {
    /**
     * 缓存
     *
     * @param goodsList
     */
    void upSeckillGoods(List<SeckillGoods> goodsList);

    /**
     * 清理缓存
     */
    void clearCache();

    /**
     * 本地缓存中查询秒杀商品列表
     *
     * @return
     */
    List<SeckillGoods> getSeckillGoodsFromLocal();

    List<SeckillGoods> getSeckillGoodsFromRemote();

    /**
     * 同步redis中缓存
     */
    void syncLocalAndRedisCache();

    /**
     * 获取当前商品
     *
     * @param skuId
     * @return
     */
    SeckillGoods getSeckillGoodsDetail(Long skuId);
}
