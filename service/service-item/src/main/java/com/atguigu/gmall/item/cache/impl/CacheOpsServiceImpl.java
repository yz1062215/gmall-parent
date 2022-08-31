package com.atguigu.gmall.item.cache.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.item.cache.CacheOpsService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 封装缓存操作
 */
@Service
@Slf4j
public class CacheOpsServiceImpl implements CacheOpsService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

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
    public boolean bloomContains(Long skuId) {
        RBloomFilter<Object> filter = redissonClient.getBloomFilter(SysRedisConst.BLOOM_SKUID);
        boolean contains = filter.contains(skuId);
        return contains;
    }

    @Override
    public boolean lock(Long skuId) {
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

    @Override
    public void unlock(Long skuId) {
        //定义锁用的key
        String lockKey=SysRedisConst.LOCK_SKU_DETAIL+skuId;
        RLock lock = redissonClient.getLock(lockKey);

        //解锁
        lock.unlock();
    }
}
