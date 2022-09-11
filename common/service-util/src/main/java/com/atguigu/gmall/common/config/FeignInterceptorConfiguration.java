package com.atguigu.gmall.common.config;

import com.atguigu.gmall.common.constant.SysRedisConst;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignInterceptorConfiguration {

    /**
     * 将网关透传过来的userId带到feign构建的新请求中
     * @return
     */
    @Bean
    public RequestInterceptor usRequestInterceptor(){

        return (template) -> {
            //springMvc   请求保持器机制
            ServletRequestAttributes attributes= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            //获取用户id
            String userId = attributes.getRequest().getHeader(SysRedisConst.USERID_HEADER);

            //将透传过来的用户id设置给feign新请求的请求头中去
            template.header(SysRedisConst.USERID_HEADER, userId);

            //对于未登录的用户前端会分配临时ID  也透穿到feign的新请求头中去
            String tempId = attributes.getRequest().getHeader(SysRedisConst.USERTEMPID_HEADER);
            template.header(SysRedisConst.USERTEMPID_HEADER,tempId);

            log.info("用户登录id: {} ,临时id {}",userId,tempId);
        };
    }
}
