package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.vo.user.UserAuthInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/inner/rpc/cart")
@RestController
@Slf4j
public class CartApiController {
    @Autowired
    CartService cartService;

    @GetMapping("/add2Cart")
    public Result<SkuInfo> add2Cart(@RequestParam("skuId") Long skuId,
                                    @RequestParam("num") Integer num
                                   ) {
        UserAuthInfo userAuthInfo = AuthUtils.getCurrentAuthInfo();
        log.info("用户id: {} ,临时id: {}",userAuthInfo.getUserId(),userAuthInfo.getUserTempId());
        SkuInfo skuInfo=cartService.addToCart(skuId,num);

        return Result.ok(skuInfo);
    }

    /**
     * 删除购物车中选中的商品
     * @return
     */
    @GetMapping("/deleteChecked")
    public Result deleteChecked(){
        String cartKey = cartService.determinCartKey();
        cartService.deleteChecked(cartKey);
        return Result.ok();
    }

    /**
     * 获取当前购物车中选中的所有商品
     * @return
     */
    @GetMapping("/getChecked")
    public Result<List<CartInfo>> getChecked(){
        String cartKey = cartService.determinCartKey();
        List<CartInfo> checkedItems = cartService.getCheckedItems(cartKey);
        return Result.ok(checkedItems);
    }
}
