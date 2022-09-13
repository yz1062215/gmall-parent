package com.atguigu.gmall.model.vo.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitVo {
    private String consignee; //收货人
    private String consigneeTel; //收件人手机号
    private String deliveryAddress; //  收货人地址
    private String paymentWay; //支付方式
    private String orderComment; //订单备注
    private List<CartInfoVoNew> orderDetailList; //订单详情
}
