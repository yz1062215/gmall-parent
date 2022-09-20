package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.service.SeckillGoodsCacheOpsService;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillGoodsCacheOpsServiceImpl implements SeckillGoodsCacheOpsService {

    @Autowired
    StringRedisTemplate redisTemplate;

    //本地缓存 map  线程安全的
    private Map<Long, SeckillGoods> goodCache = new ConcurrentHashMap<>();

    @Override
    public void upSeckillGoods(List<SeckillGoods> goodsList) {
        //根据当天时间设置key 2022-09-19
        String date = DateUtil.formatDate(new Date());
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SysRedisConst.CACHE_SECKILL_GOODS + date);
        //设置缓存过期时间    两天方便后续定时任务统计数据
        hashOps.expire(2, TimeUnit.DAYS);

        goodsList.stream()
                .forEach(seckillGoods -> {
                    //1.保存秒杀商品到redis
                    hashOps.put(seckillGoods.getSkuId() + "", Jsons.toStr(seckillGoods));

                    //2.独立保存秒杀商品的库存数量   设置一天的过期时间
                    //seckill:goods:stock:49
                    String cacheKey = SysRedisConst.CACHE_SECKILL_GOODS_STOCK + seckillGoods.getSkuId();
                    redisTemplate.opsForValue()
                            .set(cacheKey, seckillGoods.getStockCount() + "", 1, TimeUnit.DAYS);

                    //3.本地缓存
                    goodCache.put(seckillGoods.getSkuId(), seckillGoods);

                });
    }

    @Override
    public void clearCache() {
        //清除本地缓存
        goodCache.clear();
    }

    @Override
    public List<SeckillGoods> getSeckillGoodsFromLocal() {
        //本地缓存可能不存在

        //1.优先查询本地
        List<SeckillGoods> goods = goodCache.values()
                .stream()
                .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                .collect(Collectors.toList());

        //2.本地没有
        if (goods == null || goods.size() == 0) {
            //3.同步redis中数据
            syncLocalAndRedisCache();
            //4.获取本地数据
            goods = goodCache.values()
                    .stream()
                    .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                    .collect(Collectors.toList());
        }
        return goods;
    }

    /**
     * 从远程中查询缓存
     *
     * @return
     */
    @Override
    public List<SeckillGoods> getSeckillGoodsFromRemote() {
        //1.远程缓存key  seckill:goods:2022-09-19
        String cacheKey = SysRedisConst.CACHE_SECKILL_GOODS + DateUtil.formatDate(new Date());
        //2.redis中查询
        List<Object> values = redisTemplate.opsForHash()
                .values(cacheKey);

        List<SeckillGoods> goods = values.stream()
                .map(str -> Jsons.toObj(str.toString(), SeckillGoods.class))
                .sorted(Comparator.comparing(SeckillGoods::getStartTime))
                .collect(Collectors.toList());

        return goods;
    }

    /**
     * 同步redis数据到本地缓存中
     */
    @Override
    public void syncLocalAndRedisCache() {
        //1.远程查询redis中缓存
        List<SeckillGoods> goods = getSeckillGoodsFromRemote();
        //2.同步到本地缓存中
        goods.stream()
                .forEach(item -> {
                    goodCache.put(item.getSkuId(), item);
                });
    }

    /**
     * 根据商品id获取当前商品
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoodsDetail(Long skuId) {
        //1.首先从本地缓存中
        SeckillGoods goods = goodCache.get(skuId);

        //2.本地缓存没有
        if (goods == null) {
            //2.1同步redis
            syncLocalAndRedisCache();
            goods = goodCache.get(skuId);
        }
        return goods;
    }
}
