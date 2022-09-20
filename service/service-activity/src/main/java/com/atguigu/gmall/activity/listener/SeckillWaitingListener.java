package com.atguigu.gmall.activity.listener;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.mq.SeckillTempOrderMsg;
import com.atguigu.gmall.service.RabbitService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@Slf4j
public class SeckillWaitingListener {
    @Autowired
    SeckillGoodsService seckillGoodsService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RabbitService rabbitService;


    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = MqConst.QUEUE_SECKILL_ORDERWAIT, durable = "true", exclusive = "false", autoDelete = "false"), exchange = @Exchange(value = MqConst.EXCHANGE_SECKILL_EVENT, type = "topic"), key = MqConst.RK_SECKILL_ORDERWAIT)})
    public void seckillWaiting(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties()
                .getDeliveryTag();

        SeckillTempOrderMsg tempOrderMsg = Jsons.toObj(message, SeckillTempOrderMsg.class);

        log.info("监听到秒杀扣库存消息....{}", tempOrderMsg);
        Long skuId = tempOrderMsg.getSkuId();

        try {
            //数据库减库存   数据库库存字段设置了非负约束  减失败报异常
            seckillGoodsService.deduceSeckillGoods(skuId);

            //扣减库存成功后  给订单服务发消息创建订单
            rabbitTemplate.convertAndSend(MqConst.EXCHANGE_ORDER_EVNT,
                    MqConst.RK_ORDER_SECKILLOK,
                    Jsons.toStr(tempOrderMsg));

            //redis修改标志位
            String json = redisTemplate.opsForValue()
                    .get(SysRedisConst.SECKILL_ORDER + tempOrderMsg.getSkuCode());
            OrderInfo orderInfo = Jsons.toObj(json, OrderInfo.class);

            //设置订单操作时间表示库存扣减操作过了
            orderInfo.setOperateTime(new Date());
            redisTemplate.opsForValue()
                    .set(SysRedisConst.SECKILL_ORDER + tempOrderMsg.getSkuCode(), Jsons.toStr(orderInfo));

            channel.basicAck(tag, false);
        } catch (DataIntegrityViolationException e) {
            log.error("扣库存失败：{}", e);
            //扣库存失败。redis临时单改成 x 错误标志。
            redisTemplate.opsForValue()
                    .set(SysRedisConst.SECKILL_ORDER + tempOrderMsg.getSkuCode(), "x");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("业务失败：{}", e);
            rabbitService.retryConsumerMsg(10L, SysRedisConst.MQ_RETRY + tempOrderMsg.getSkuCode(), tag, channel);
        }


    }
}
