package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.payment.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author yzz
* @description 针对表【payment_info(支付信息表)】的数据库操作Service
* @createDate 2022-09-12 12:58:36
*/
public interface PaymentInfoService extends IService<PaymentInfo> {

    PaymentInfo savePaymentInfo(Map<String, String> map);
}
