package com.atguigu.gmall.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.payment.config.AlipayProperties;
import com.atguigu.gmall.payment.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayServiceImpl implements AlipayService {


    @Autowired
    OrderFeignClient orderFeignClient;
    @Autowired
    AlipayClient alipayClient;
    @Autowired
    AlipayProperties properties;
    /**
     * 获取支付宝支付页面
     * @param orderId
     * @return
     */
    @Override
    public String getAlipayHtml(Long orderId) throws AlipayApiException {
        //1.远程调用获取订单信息
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId).getData();

        //对订单过期时间支付宝进行同步
        if (orderInfo.getExpireTime().before(new Date())){
            throw new GmallException(ResultCodeEnum.ORDER_EXPIRE);
        }

        //2.创建支付请求
        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        //3.构造支付宝跳转请求需要的参数
        payRequest.setReturnUrl(properties.getReturnUrl());

        //4.支付宝给给唤醒页面发送成功支付通知
        payRequest.setNotifyUrl(properties.getNotifyUrl());

        //5.构造支付数据
        Map<String,String> bizContent=new HashMap<>();
        //设置订单对外交易号。  这个也是唯一识别订单。
        bizContent.put("out_trade_no",orderInfo.getOutTradeNo());
        bizContent.put("total_amount",orderInfo.getTotalAmount().toString());
        bizContent.put("subject","尚品汇订单-"+orderInfo.getOutTradeNo());
        bizContent.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContent.put("body",orderInfo.getTradeBody());
        //绝对超时
        String date = DateUtil.formatDate(orderInfo.getExpireTime(), "yyyy-MM-dd HH:mm:ss");
        bizContent.put("time_expire", date);

        //请求参数设置给支付请求
        payRequest.setBizContent(Jsons.toStr(bizContent));

        String result = alipayClient.pageExecute(payRequest).getBody();


        return result;
    }

    /**
     * 验签
     * @param paramMaps
     * @return
     * @throws AlipayApiException
     *
     * Params:
     * params – 待验签的从支付宝接收到的参数Map
     * publicKey – 支付宝公钥
     * charset – 参数内容编码集
     * signType – 指定采用的签名方式，RSA或RSA2
     */
    @Override
    public boolean rsaCheckV1(Map<String, String> paramMaps) throws AlipayApiException {
       boolean b= AlipaySignature.rsaCheckV1(paramMaps,//参数列表
                properties.getAlipayPublicKey(),//公钥
                properties.getCharset(),//字符编码
                properties.getSignType());//签名类型
        return b;
    }
}
