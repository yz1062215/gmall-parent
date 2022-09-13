package com.atguigu.gmall.user.api;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/inner/rpc/user")
@RestController
public class UserApiController {

    @Autowired
    UserAddressService userAddressService;

    @GetMapping("/getUserAddress/list")
    public Result<List<UserAddress>> getUserAddress() {
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId);
        List<UserAddress> list = userAddressService.list(wrapper);
        return Result.ok(list);
    }
}
