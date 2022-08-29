package com.atguigu.gmall.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTest {
    //@Autowired
    //RedisTemplate redisTemplate;
    //Template  中间件的工具操作类
    @Autowired
    StringRedisTemplate redisTemplate;
    @Test
    public void Test01(){
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("hehe", "haha");
        System.out.println("save success......");
        String s = ops.get("hehe");
        System.out.println("s = " + s);
    }
}
