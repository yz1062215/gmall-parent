package com.atguigu.gmall.order.service.impl;


import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.mapper.PaymentInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
* @author yzz
* @description 针对表【payment_info(支付信息表)】的数据库操作Service实现
* @createDate 2022-09-12 12:58:36
*/
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
    implements PaymentInfoService {
    @Autowired
    OrderInfoService orderInfoService;
    /**
     * 保存支付消息
     * @param map
     * @return
     */
    @Transactional
    @Override
    public PaymentInfo savePaymentInfo(Map<String, String> map) {
        PaymentInfo paymentInfo = new PaymentInfo();
        //1.对外交易号
        String outTradeNo = map.get("out_trade_no");
        paymentInfo.setOutTradeNo(outTradeNo);

        //2.用户id  截取对外流水号 _ 后的内容
        long userId = Long.parseLong(outTradeNo.split("_")[1]);
        paymentInfo.setUserId(userId);

        //判断这个定单数据是否已经保存过
        PaymentInfo one = getOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getUserId, userId)
                .eq(PaymentInfo::getOutTradeNo, outTradeNo));

        if (one!=null){ //订单存在
            return one;
        }
        //3.订单id
        OrderInfo orderInfo = orderInfoService
                .getOrderInfoByOutTradeNoAndUserId(outTradeNo,paymentInfo.getUserId());
        paymentInfo.setOrderId(orderInfo.getId());
        //4.支付方式
        paymentInfo.setPaymentType("ALIPAY");
        //5.支付宝流水号
        paymentInfo.setTradeNo(map.get("trade_no"));
        paymentInfo.setTotalAmount(new BigDecimal(map.get("total_amount")));
        paymentInfo.setSubject(map.get("subject"));

        paymentInfo.setPaymentStatus(map.get("trade_status"));
        paymentInfo.setCreateTime(new Date());

        Date callbackTime = DateUtil.parseDate(map.get("notify_time"),"yyyy-MM-dd HH:mm:ss");
        paymentInfo.setCallbackTime(callbackTime);


        //回调内容
        paymentInfo.setCallbackContent(Jsons.toStr(map));

        save(paymentInfo);
        return paymentInfo;
    }
}




