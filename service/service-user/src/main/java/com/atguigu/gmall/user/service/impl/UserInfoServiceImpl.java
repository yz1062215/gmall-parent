package com.atguigu.gmall.user.service.impl;


import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.model.vo.user.LoginSuccessVo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
* @author yzz
* @description 针对表【user_info(用户表)】的数据库操作Service实现
* @createDate 2022-09-07 07:43:52
*/
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService{
    @Autowired
    StringRedisTemplate redisTemplate;
    @Resource
    UserInfoMapper userInfoMapper;
    @Override
    public LoginSuccessVo login(UserInfo userInfo) {

        LoginSuccessVo vo = new LoginSuccessVo();
        LambdaQueryWrapper<UserInfo> wrapper =new LambdaQueryWrapper<>();
        //if (userInfo.getLoginName()!=null){
            wrapper.eq(UserInfo ::getLoginName,userInfo.getLoginName()).
                    eq(UserInfo::getPasswd, MD5.encrypt(userInfo.getPasswd()));
        //}else {
        //    wrapper.eq(UserInfo::getName,userInfo.getName()).
        //            eq(UserInfo::getPasswd,MD5.encrypt(userInfo.getPasswd()));
        //}
        //查询数据库 验证密文密码与账号是否正确
        UserInfo login = userInfoMapper.selectOne(wrapper);
        //判断登录状态
        //登录时要求：  账号+密码  或者 用户名加密码
        if (login != null) {
            //登录成功  生成token
            String token = UUID.randomUUID().toString().replace("-", "");
            //存入redis
            redisTemplate.opsForValue().set(SysRedisConst.LOGIN_USER+token,
                    Jsons.toStr(login),
                    7, TimeUnit.DAYS);
            //返回用户昵称和token
            vo.setNickName(login.getNickName());
            vo.setToken(token);
            return vo;
        }
        return null;
    }

    /**
     * 退出登录
     * @param token
     */
    @Override
    public void logout(String token) {
        redisTemplate.delete(token);
    }
}




