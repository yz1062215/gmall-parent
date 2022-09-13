package com.atguigu.gmall.feign.order;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-order")
@RequestMapping("/api/inner/rpc/order")
public interface OrderFeignClient {
    /**
     * 提交订单
     * @param tradeNo
     * @return
     */
    @PostMapping("/auth/submitOrder")
     Result<OrderDetail> submitOrder(@RequestParam("tradeNo") Long tradeNo);
}
