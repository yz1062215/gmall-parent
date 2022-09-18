package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.model.to.mq.WareDeduceMsg;
import com.atguigu.gmall.model.to.mq.WareDeduceSkuInfo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.service.PaymentInfoService;
import com.atguigu.gmall.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OrderPayedListener {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RabbitService rabbitService;
    @Autowired
    PaymentInfoService paymentInfoService;
    @Autowired
    OrderInfoService orderInfoService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    OrderDetailService orderDetailService;

    @RabbitListener(queues = MqConst.QUEUE_ORDER_PAYED)
    public void payedListener(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();

        //1.支付宝异步回调数据转为map
        Map<String,String> map = Jsons.toObj(message, Map.class);
        String trade_no = map.get("trade_no");
        try {
            //处理业务  修改订单状态
            //3、保存支付消息。为了让保存幂等
            //3.1、先查询，再保存
            //3.2、使用唯一约束
            PaymentInfo info =paymentInfoService.savePaymentInfo(map);
            //4.修改订单状态
            Long orderId = info.getOrderId();
            Long userId = info.getUserId();
            //将未支付改为已支付
            orderInfoService.changeOrderStatus(orderId,userId,
                    ProcessStatus.PAID,
                    Arrays.asList(ProcessStatus.UNPAID,ProcessStatus.CLOSED));

            //扣库存
            WareDeduceMsg msg = prepareWareDeduceMsg(info);
            rabbitTemplate.convertAndSend(MqConst.EXCHANGE_WARE_EVENT,
                    MqConst.RK_WARE_DEDUCE,Jsons.toStr(msg));
            channel.basicAck(tag,false);

        }catch (Exception e) {
            String uniqKey= SysRedisConst.MQ_RETRY+"order:payed:"+trade_no;
            rabbitService.retryConsumerMsg(10L,uniqKey ,tag,channel );
        }
    }

    /**
     * 扣库存
     * @param info
     * @return
     */
    private WareDeduceMsg prepareWareDeduceMsg(PaymentInfo info) {
        WareDeduceMsg msg = new WareDeduceMsg();
        Long userId = info.getUserId();
        msg.setOrderId(info.getOrderId());

        //查询当前订单信息
        OrderInfo orderInfo=orderInfoService.getOrderInfoByOrderIdAndUserId(info.getOrderId(),userId);
        msg.setConsignee(orderInfo.getConsignee());
        msg.setConsigneeTel(orderInfo.getConsigneeTel());
        msg.setOrderComment(orderInfo.getOrderComment());
        msg.setOrderBody(orderInfo.getTradeBody());
        msg.setDeliveryAddress(orderInfo.getDeliveryAddress());
        msg.setPaymentWay("2");


        //订单详情
        List<WareDeduceSkuInfo> infos = orderDetailService.list(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderInfo.getId())
                .eq(OrderDetail::getUserId, userId))
                .stream().map(orderDetail -> {
                    WareDeduceSkuInfo skuInfo = new WareDeduceSkuInfo();
                    skuInfo.setSkuId(orderDetail.getSkuId());
                    skuInfo.setSkuNum(orderDetail.getSkuNum());
                    skuInfo.setSkuName(orderDetail.getSkuName());
                    return skuInfo;
                }).collect(Collectors.toList());



        msg.setDetails(infos);


    return msg;

    }
}
