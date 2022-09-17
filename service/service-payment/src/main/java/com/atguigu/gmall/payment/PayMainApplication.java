package com.atguigu.gmall.payment;


import com.atguigu.gmall.common.annotation.EnableAutoFeignInterceptor;
import com.atguigu.gmall.common.annotation.EnableGmallGlobalException;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableAutoFeignInterceptor //开启自定义feign拦截器
@EnableGmallGlobalException //开启自定义全局异常处理器
@EnableFeignClients(basePackages = {"com.atguigu.gmall.feign.order"})
public class PayMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayMainApplication.class, args);
    }
}
