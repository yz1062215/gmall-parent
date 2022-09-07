package com.atguigu.gmall.user.service;


import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.model.vo.user.LoginSuccessVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yzz
* @description 针对表【user_info(用户表)】的数据库操作Service
* @createDate 2022-09-07 07:43:52
*/
public interface UserInfoService extends IService<UserInfo> {
    /**
     * 用户登录
     *
     * @param userInfo
     * @return
     */
    LoginSuccessVo login(UserInfo userInfo);

    /**
     * 删除
     * @param token
     */
    void logout(String token);
}
