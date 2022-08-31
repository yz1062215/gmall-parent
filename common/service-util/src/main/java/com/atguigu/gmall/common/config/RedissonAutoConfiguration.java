package com.atguigu.gmall.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 给容器中放好RedissonClient组件
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
public class RedissonAutoConfiguration {

    @Autowired
    RedisProperties redisProperties;

    @Bean
    public RedissonClient redissonClient(){
        //1.创建一个配置
        Config config = new Config();
        String host = redisProperties.getHost();
        int port = redisProperties.getPort();
        String password = redisProperties.getPassword();
        //2.指定redisson的配置项
        config.useSingleServer()
                .setAddress("redis://"+host+":"+port)
                .setPassword(password);

        //3.创建Redisson客户端
        RedissonClient client = Redisson.create(config);
        return client;
    }
}
