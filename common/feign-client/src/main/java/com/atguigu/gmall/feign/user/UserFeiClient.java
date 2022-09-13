package com.atguigu.gmall.feign.user;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-user")
@RequestMapping("/api/inner/rpc/user")
public interface UserFeiClient {

    @PostMapping("/passport/login")
    Result login(@RequestBody UserInfo userInfo);

    /**
     * 获取用户所有地址
     * @return
     */
    @GetMapping("/getUserAddress/list")
    Result<List<UserAddress>> getUserAddress();
}
