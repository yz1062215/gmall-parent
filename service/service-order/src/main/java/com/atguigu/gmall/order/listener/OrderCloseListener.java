package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.to.mq.OrderMsg;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OrderCloseListener {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderBizService orderBizService;

    @RabbitListener(queues = MqConst.QUEUE_ORDER_DEAD)
    public void orderClose(Message message, Channel channel) throws IOException {
        long tag=message.getMessageProperties().getDeliveryTag();

        //1.获取订单信息
        OrderMsg msg = Jsons.toObj(message, OrderMsg.class);

        try {
            //2.进行订单关闭
            log.info("监听到超时订单{}，正在关闭",msg);
            orderBizService.closeOrder(msg.getOrderId(),msg.getUserId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("订单关闭业务失败。消息：{}，失败原因：{}",msg,e);

            Long a = redisTemplate.opsForValue().increment(SysRedisConst.MQ_RETRY + "order:" + msg.getOrderId());//redis设置计数限制
            if(a<=10){
                //如果redis小于十次则将消息返回队列
                channel.basicNack(tag, false, true);
            }else {
                //超过10次则不入队  删除redis中缓存
                channel.basicNack(tag,false, false);
                redisTemplate.delete(SysRedisConst.MQ_RETRY+"order:"+msg.getOrderId());
            }
        }
    }
}
