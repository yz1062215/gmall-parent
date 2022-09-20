package com.atguigu.gmall.activity;

import com.atguigu.gmall.annotation.EnableAppRabbit;
import com.atguigu.gmall.common.annotation.EnableGmallGlobalException;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringCloudApplication
@MapperScan("com.atguigu.gmall.activity.mapper")
@EnableGmallGlobalException//开启全局异常处理
@EnableAppRabbit //开启自定义mq注解
@EnableScheduling //开启定时任务
public class ActivityMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActivityMainApplication.class, args);
    }
}
