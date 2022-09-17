package com.atguigu.gmall.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付客户端配置
 */
@Configuration
public class AliPayConfiguration {

    @Bean
    public AlipayClient alipayClient(AlipayProperties properties){
        DefaultAlipayClient alipayClient = new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getMerchantPrivateKey(),
                "json",properties.getCharset(),
                properties.getAlipayPublicKey(),
                properties.getSignType()
                );
        return alipayClient;
    }
}
