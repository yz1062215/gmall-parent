package com.atguigu.gmall.common.annotation;


import com.atguigu.gmall.common.handler.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(GlobalExceptionHandler.class)
//1、导入 AppThreadPoolAutoConfiguration 组件。
//2、开启 @EnableConfigurationProperties(AppThreadPoolProperties.class) 这个配置
//     - 和配置文件绑好
//     - AppThreadPoolProperties 放到容器
//3、AppThreadPoolAutoConfiguration 给容器中放一个 ThreadPoolExecutor
//效果： 随时 @Autowired ThreadPoolExecutor即可，也很方便改配置
public @interface EnableGmallGlobalException {
}
