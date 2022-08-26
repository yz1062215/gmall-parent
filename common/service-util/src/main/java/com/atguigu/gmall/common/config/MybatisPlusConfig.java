package com.atguigu.gmall.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MybatisPlus配置类
 *
 */
@EnableTransactionManagement
@Configuration
@MapperScan("com.atguigu.gmall.*.mapper")
public class MybatisPlusConfig {

    /**
     * 分页插件
     */
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

//    @Bean
//    public ISqlInjector sqlInjector() {
//        return new LogicSqlInjector();
//    }
//
//    /**
//     * SQL执行效率插件
//     */
//
//    @Bean
//    @Profile({"dev","test"})// 设置 dev test 环境开启
//    public PerformanceInterceptor performanceInterceptor() {
//        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
//        performanceInterceptor.setMaxTime(2000);
//        performanceInterceptor.setFormat(true);
//        return performanceInterceptor;
//    }
}
