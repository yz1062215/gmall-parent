package com.atguigu.gmall.activity.schedule;


import com.atguigu.gmall.activity.service.SeckillGoodsCacheOpsService;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.model.activity.SeckillGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务  上架当天参与秒杀的商品
 */
@Service
@Slf4j
public class SeckillGoodsUpService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    SeckillGoodsService seckillGoodsService;
    @Autowired
    SeckillGoodsCacheOpsService cacheOpsService;

    //    @Scheduled(cron = "0 0 2 * * ?")
    @Scheduled(cron = "0 * * * * ?")
    public void upSeckillGoods() {
        log.info("上架参与秒杀的商品.......");

        //1.查询当天参与秒杀的所有商品
        List<SeckillGoods> goodsList = seckillGoodsService.getCurrentDaySeckillGoodsList();

        //2.redis缓存当天秒杀的商品  +  本地缓存商品  + redis缓存库存
        cacheOpsService.upSeckillGoods(goodsList);

    }

    /**
     * 清除本地缓存  防止OOM 每晚一点
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void currentDaySeckillEnd() {

        cacheOpsService.clearCache();
    }

}
