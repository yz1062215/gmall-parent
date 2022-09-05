package com.atguigu.gmall.product;

import com.atguigu.gmall.common.annotation.EnableThreadPool;
import com.atguigu.gmall.common.config.Swagger2Config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringCloudApplication
@MapperScan("com.atguigu.gmall.product.mapper")
@Import({Swagger2Config.class})
@EnableThreadPool
//@EnableRedisson
@EnableScheduling//开启定时任务重构布隆过滤器
@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign.search"
})
public class ProductMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductMainApplication.class,args);
    }
}
