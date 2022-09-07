package com.atguigu.gmall.user.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.model.vo.user.LoginSuccessVo;
import com.atguigu.gmall.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/user")
@RestController
public class UserController {
    @Autowired
    UserInfoService userInfoService;

    /**
     * 登录
     * @param userInfo
     * @return
     */
    @PostMapping("/passport/login")
    public Result login(@RequestBody UserInfo userInfo){
        LoginSuccessVo login= userInfoService.login(userInfo);
        if (login!=null){
            return Result.ok(login);
        }
        return Result.build("", ResultCodeEnum.LOGIN_ERROR);

    }

    @GetMapping("/passport/logout")
    public Result logout(@RequestHeader("token")String token){
        userInfoService.logout(token);
        return Result.ok();
    }
}
