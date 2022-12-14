package com.atguigu.starter.cache.service.impl;


import com.atguigu.starter.cache.constant.SysRedisConst;
import com.atguigu.starter.cache.service.CacheOpsService;
import com.atguigu.starter.cache.utils.Jsons;
import com.fasterxml.jackson.core.type.TypeReference;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 封装缓存操作
 */
@Service
//@Slf4j
public class CacheOpsServiceImpl implements CacheOpsService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

    ScheduledExecutorService scheduledExecutor= Executors.newScheduledThreadPool(4);//创建定时调度线程池

    /**
     * 从缓存中获取一个数据，并转成指定类型的对象
     * @param cacheKey
     * @param clz
     * @return
     * @param <T>
     */
    @Override
    public <T> T getCacheData(String cacheKey, Class<T> clz) {
        String jsonStr = redisTemplate.opsForValue().get(cacheKey);
        //如果为空值缓存
        if (SysRedisConst.NULL_VALUE.equals(jsonStr)){
            return null;
        }
        T t = Jsons.toObj(jsonStr, clz);
        return t;
    }

    @Override
    public Object getCacheData(String cacheKey, Type type) {
        String jsonStr = redisTemplate.opsForValue().get(cacheKey);
        //引入null值缓存机制。
        if(SysRedisConst.NULL_VALUE.equals(jsonStr)){
            return null;
        }

        //逆转json为Type类型的复杂对象
        Object obj = Jsons.toObj(jsonStr, new TypeReference<Object>() {
            @Override
            public Type getType() {
                return type; //这个是方法的带泛型的返回值类型
            }
        });
        return obj;
    }

    @Override
    public boolean bloomContains(Object skuId) {
        RBloomFilter<Object> filter = redissonClient.getBloomFilter(SysRedisConst.BLOOM_SKUID);
        boolean contains = filter.contains(skuId);
        return contains;
    }

    @Override
    public boolean bloomContains(String bloomName, Object bVal) {
        RBloomFilter<Object> filter = redissonClient.getBloomFilter(bloomName);
        return filter.contains(bVal);
    }


    @Override
    public boolean tryLock(String lockName) {
        RLock rLock = redissonClient.getLock(lockName);
        return rLock.tryLock();
    }

    @Override
    public boolean tryLock(Long skuId) {
        //定义锁用的key
        String lockKey=SysRedisConst.LOCK_SKU_DETAIL+skuId;
        RLock lock = redissonClient.getLock(lockKey);
        //尝试加锁
        boolean b = lock.tryLock();

        return b;
    }

    @Override
    public void saveData(String cacheKey, Object fromRpc) {
        if (fromRpc == null) {
            //空值缓存  如果为空值就设置30分钟的空缓存
            redisTemplate.opsForValue().set(cacheKey, SysRedisConst.NULL_VALUE, SysRedisConst.NULL_VALUE_TTL, TimeUnit.SECONDS);
        } else {
            //不为空值缓存
            String str = Jsons.toStr(fromRpc);//查询到的数据为Json 转为string  可能为空值缓存
            redisTemplate.opsForValue().set(cacheKey,
                    str,
                    SysRedisConst.SKUDETAIL_TTL,
                    TimeUnit.SECONDS);//七天
        }
    }

    /**
     * 保存缓存的指定时间
     * @param cacheKey
     * @param fromRpc
     * @param dataTtl
     */
    @Override
    public void saveData(String cacheKey, Object fromRpc, Long dataTtl) {
        if (fromRpc == null) {
            //空值缓存  如果为空值就设置30分钟的空缓存
            redisTemplate.opsForValue().set(cacheKey, SysRedisConst.NULL_VALUE, SysRedisConst.NULL_VALUE_TTL, TimeUnit.SECONDS);
        } else {
            //不为空值缓存
            String str = Jsons.toStr(fromRpc);//查询到的数据为Json 转为string  可能为空值缓存
            redisTemplate.opsForValue().set(cacheKey,
                    str,
                    dataTtl,
                    TimeUnit.SECONDS);//七天
        }

    }


    @Override
    public void unlock(Long skuId) {
        //定义锁用的key
        String lockKey=SysRedisConst.LOCK_SKU_DETAIL+skuId;
        RLock lock = redissonClient.getLock(lockKey);

        //解锁
        lock.unlock();
    }

    @Override
    public void unlock(String lockName) {
        RLock lock = redissonClient.getLock(lockName);
        lock.unlock(); //redisson已经防止了删别人锁
    }

    /**
     * 异步双删
     * @param cacheKey
     */
    @Override
    public void delay2Delete(String cacheKey) {
        redisTemplate.delete(cacheKey);

        //异步延迟任务   结合后台管理系统，专门准备清空缓存的按钮功能
        scheduledExecutor.schedule(()->{
            redisTemplate.delete(cacheKey);
        }, 5, TimeUnit.SECONDS);//延时五秒再次删除缓存

    }
}
