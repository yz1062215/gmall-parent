package com.atguigu.gmall.web;


import com.atguigu.gmall.common.annotation.EnableAutoFeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * 不要启用数据源的自动配置
 * 1、DataSourceAutoConfiguration 就会生效
 *
 * 前端项目-页面跳转与数据渲染（thymeleaf）
 *
 */
//@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
//@EnableDiscoveryClient
//@EnableCircuitBreaker

@EnableFeignClients(basePackages = {
        "com.atguigu.gmall.feign.item",
        "com.atguigu.gmall.feign.product",
        "com.atguigu.gmall.feign.search",
        "com.atguigu.gmall.feign.user",
        "com.atguigu.gmall.feign.cart",
        "com.atguigu.gmall.feign.order"
}) //只会扫描主程序所在的子包
@EnableAutoFeignInterceptor
@SpringCloudApplication
public class WebAllMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAllMainApplication.class,args);
    }
}
