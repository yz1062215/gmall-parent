package com.atguigu.gmall.order;

import com.atguigu.gmall.annotation.EnableAppRabbit;
import com.atguigu.gmall.common.annotation.EnableAutoFeignInterceptor;
import com.atguigu.gmall.common.annotation.EnableGmallGlobalException;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAppRabbit //开启自定义rabbit
@SpringCloudApplication
@EnableTransactionManagement //开启事务
@EnableGmallGlobalException//异常处理机制
@MapperScan("com.atguigu.gmall.order.mapper")
@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign.cart",
        "com.atguigu.gmall.feign.user",
        "com.atguigu.gmall.feign.product",
        "com.atguigu.gmall.feign.ware"
})
@EnableAutoFeignInterceptor //开启拦截器透传
public class OrderMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderMainApplication.class, args);
    }
}
