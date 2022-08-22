package com.atguigu.gmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/*
主启动类
 */
//@SpringBootApplication
//@EnableDiscoveryClient//服务注册发现
//@EnableCircuitBreaker//熔断降级和流量保护
@SpringCloudApplication //三合一
public class GatewayMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayMainApplication.class, args);
    }
}
