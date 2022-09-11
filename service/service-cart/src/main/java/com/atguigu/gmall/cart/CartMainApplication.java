package com.atguigu.gmall.cart;

import com.atguigu.gmall.common.annotation.EnableAutoFeignInterceptor;
import com.atguigu.gmall.common.annotation.EnableGmallGlobalException;
import com.atguigu.gmall.common.annotation.EnableThreadPool;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableThreadPool //自定义线程池
@EnableGmallGlobalException
@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign.product"
})
@EnableAutoFeignInterceptor
public class CartMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartMainApplication.class,args);
    }
}
