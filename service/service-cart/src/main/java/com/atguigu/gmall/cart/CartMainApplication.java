package com.atguigu.gmall.cart;

import com.atguigu.gmall.common.annotation.EnableGmallGlobalException;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableGmallGlobalException
@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign.product"
})
public class CartMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartMainApplication.class,args);
    }
}
