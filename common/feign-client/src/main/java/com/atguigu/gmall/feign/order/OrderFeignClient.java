package com.atguigu.gmall.feign.order;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-order")
@RequestMapping("/api/inner/rpc/order")
public interface OrderFeignClient {
    /**
     * 提交订单
     *
     * @param tradeNo
     * @return
     */
    @PostMapping("/auth/submitOrder")
    Result<OrderDetail> submitOrder(@RequestParam("tradeNo") Long tradeNo);

    @GetMapping("/confirm/data")
    Result<OrderConfirmDataVo> confirmOrderInfo();

    @GetMapping("/info/{orderId}")
    public Result<OrderInfo> getOrderInfo(@PathVariable("orderId") Long orderId);

    /**
     * 保存秒杀订单
     *
     * @param info
     * @return
     */
    @PostMapping("/seckillorder/submit")
    public Result<Long> submitSeckillOrder(@RequestBody OrderInfo info);
}
