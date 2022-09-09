package com.atguigu.gmall.common.auth;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.model.vo.user.UserAuthInfo;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 利用线程绑定机制获取透过来的id  工具类
 */
public class AuthUtils {

    /**
     * 利用tomcat线程绑定机制  +Spring 请求上下文保持机制  ThreadLocal原理=同一个请求在处理期间随时调用
     * @return
     */
    public static UserAuthInfo getCurrentAuthInfo() {
        UserAuthInfo userAuthInfo = new UserAuthInfo();

        ServletRequestAttributes attributes= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String userId = attributes.getRequest().getHeader(SysRedisConst.USERID_HEADER);
        String tempId = attributes.getRequest().getHeader(SysRedisConst.USERTEMPID_HEADER);
        if (!StringUtils.isEmpty(userId)){
            userAuthInfo.setUserId(Long.parseLong(userId));
        }

        userAuthInfo.setUserTempId(tempId);
        return userAuthInfo;


    }
}
