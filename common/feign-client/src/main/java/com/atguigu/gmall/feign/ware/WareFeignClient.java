package com.atguigu.gmall.feign.ware;

import com.atguigu.gmall.feign.ware.callback.WareFeignClientCallbackImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "ware-mange",
        url = "${app.ware-url:http://localhost:10001/}",
        fallback = WareFeignClientCallbackImpl.class)
public interface WareFeignClient {
    /**
     * 查询商品库存是否充足
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/hasStock")
    String hasStock(@RequestParam("skuId") Long skuId,
                    @RequestParam("num") Integer num);
}
