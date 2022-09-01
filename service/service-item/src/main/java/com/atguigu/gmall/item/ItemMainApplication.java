package com.atguigu.gmall.item;

import com.atguigu.gmall.common.annotation.EnableRedisson;
import com.atguigu.gmall.common.annotation.EnableThreadPool;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringCloudApplication
@EnableFeignClients
//@Import(AppThreadPoolAutoConfig.class)
@EnableThreadPool
@EnableRedisson
@EnableAspectJAutoProxy//开启aspectj的自动代理功能.  可以给任意类创建代理对象
public class ItemMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemMainApplication.class, args);
    }
}
