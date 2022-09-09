package com.atguigu.gmall.feign.cart;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-cart")
@RequestMapping("/api/inner/rpc/cart")
public interface CartFeignClient {
    /**
     * 添加指定数目的某个商品到购物车
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/add2Cart")
    public Result<SkuInfo> add2Cart(@RequestParam("skuId") Long skuId,
                                    @RequestParam("num") Integer num
                                   );

    /**
     * 删除购物车选中的商品
     * @return
     */
    @GetMapping("/deleteChecked")
     Result deleteChecked();
}
