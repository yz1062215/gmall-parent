package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {

    @Autowired
    OrderFeignClient orderFeignClient;

    @GetMapping("trade.html")
    public String trade(Model model) {
        //detailArrayList
        Result<OrderConfirmDataVo> result = orderFeignClient.confirmOrderInfo();
        if (result.isOk()) {
            model.addAttribute("detailArrayList", result.getData().getDetailArrList());
            model.addAttribute("totalNum", result.getData().getTotalNum());
            model.addAttribute("totalAmount", result.getData().getTotalAmount());
            model.addAttribute("userAddressList", result.getData().getUserAddressesList());
            model.addAttribute("tradeNo", result.getData().getTradeNo()); //订单交易追踪号
        }
        return "order/trade";
    }
}


