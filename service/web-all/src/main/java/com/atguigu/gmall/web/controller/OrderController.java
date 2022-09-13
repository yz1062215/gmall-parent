package com.atguigu.gmall.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {


    @GetMapping("trade.html")
    public String trade(Model model){
        //detailArrayList

        model.addAttribute("detailArrayList",null);
        model.addAttribute("totalNum",0);
        model.addAttribute("totalAmount",0);
        model.addAttribute("userAddressList",null);
        return "order/trade";
    }

}
