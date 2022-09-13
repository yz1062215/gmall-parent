package com.atguigu.gmall.order.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.order.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单api
 */
@RestController
@RequestMapping("/api/inner/rpc/order")
public class OrderApiController {
    @Autowired
    OrderDetailService orderDetailService;

    //http://api.gmall.com/api/order/auth/submitOrder?tradeNo=null
    /**
     * 提交订单
     * @param tradeNo
     * @return
     */
    @PostMapping("/auth/submitOrder")
    public Result submitOrder(@RequestParam("tradeNo") Long tradeNo){

        List<OrderDetail> orderDetailList= orderDetailService.submitOrder(tradeNo);

        return Result.ok(orderDetailList);
    }
    //确认订单
    public Result confirmOrderInfo(){
        return Result.ok();
    }
}
