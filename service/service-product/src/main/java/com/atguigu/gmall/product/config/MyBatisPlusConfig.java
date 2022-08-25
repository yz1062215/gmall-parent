package com.atguigu.gmall.product.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement//开启事务注解支持
//分页拦截器
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor interceptor(){
        //插件主体
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        //加入内部的小插件
        PaginationInnerInterceptor innerInterceptor = new PaginationInnerInterceptor();
        //分页插件
        interceptor.addInnerInterceptor(innerInterceptor);
        return interceptor;
    }
}
