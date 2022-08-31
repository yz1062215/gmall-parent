package com.atguigu.gmall.item.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 使用redis实现分布式锁
 */
@Service
public class RedisDistLock {
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @return
     */
    public String lock() {
        String token = UUID.randomUUID().toString();
        //1.加锁
        //2.设置过期时间
        //setnxex
        while (!redisTemplate.opsForValue().setIfAbsent("lock", token, 10, TimeUnit.SECONDS)){//如果加锁失败
            //自旋阻塞加锁
        }

        //加锁成功
        return token;


    }

    public void unlock(String token) {
        String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1]  then return redis.call('del',KEYS[1]); else  return 0;end;";

        Long lock = redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Arrays.asList("lock"), token);
    }


}
