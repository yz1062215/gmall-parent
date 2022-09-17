package com.atguigu.gmall.payment.controller;


import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.payment.service.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    @Autowired
    AlipayService alipayService;


    /**
     * 买家账号jxskja5070@sandbox.com
     * 登录密码111111
     * 支付密码111111
     * 跳转到支付宝二维码收银台
     */
    @GetMapping("/alipay/submit/{orderId}")
    @ResponseBody //返回字符串或者json
    public String alipayPage(@PathVariable("orderId")Long orderId) throws AlipayApiException {

        String result=alipayService.getAlipayHtml(orderId);

        return result;
    }

    /**
     * 支付成功后的跳转
     *
     */
    @GetMapping("/paysuccess")
    public String paySuccess(@RequestParam Map<String,String> paramMaps) throws AlipayApiException {
        System.out.println("支付成功同步通知页： 收到的参数："+paramMaps);
        boolean b=alipayService.rsaCheckV1(paramMaps);
        if (b){
            //验签通过
            System.out.println("正在修改订单信息......"+paramMaps);
        }

        //验签


        //重定向到支付成功页面
        return "redirect:http://gmall.com/pay/success.html";
    }

    @ResponseBody
    @RequestMapping("/success/notify")
    public String notifySuccess(@RequestParam Map<String,String> paramMaps) throws AlipayApiException {
        boolean b = alipayService.rsaCheckV1(paramMaps);
        if (b) {
            //验签通过 异步打印日志
            log.info("异步通知抵达。支付成功，验签通过。数据：{}", Jsons.toStr(paramMaps));
        }else {
            return "error";
        }
        return "success";
    }

}
