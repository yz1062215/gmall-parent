package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

import java.util.Map;

public interface AlipayService {
    String getAlipayHtml(Long orderId) throws AlipayApiException;

    boolean rsaCheckV1(Map<String, String> paramMaps) throws AlipayApiException;

    /**
     * 支付成功后发送消息给库存服务
     * @param paramMaps
     */
    void sendPayedMsg(Map<String, String> paramMaps);
}
