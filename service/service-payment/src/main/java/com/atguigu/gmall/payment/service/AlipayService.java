package com.atguigu.gmall.payment.service;

import com.alipay.api.AlipayApiException;

import java.util.Map;

public interface AlipayService {
    String getAlipayHtml(Long orderId) throws AlipayApiException;

    boolean rsaCheckV1(Map<String, String> paramMaps) throws AlipayApiException;
}
