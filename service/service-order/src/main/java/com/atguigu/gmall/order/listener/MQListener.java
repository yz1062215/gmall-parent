package com.atguigu.gmall.order.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class MQListener {

    private ConcurrentHashMap<Long, AtomicInteger> counts=new ConcurrentHashMap<>();

    //@RabbitListener(queues = "hehe")
    //public void LisenerQueue(Message message, Channel channel) throws IOException {
    //    System.out.println("监听到的队列："+message);
    //    //byte[] content = message.getBody();
    //    //long deliveryTag = message.getMessageProperties().getDeliveryTag();
    //    //counts.putIfAbsent(deliveryTag,new AtomicInteger(0));
    //    //try {
    //    //    System.out.println("content = " + content);
    //    //    //int i = 10/0;
    //    //    channel.basicAck(deliveryTag, false);//关闭批量确认
    //    //} catch (IOException e) {
    //    //    //对于消费失败的消息  打印错误信息和错误消息
    //    //    log.error("消息消费失败： {}"+content);
    //    //
    //    //    AtomicInteger atomicInteger = counts.get(deliveryTag);
    //    //    System.out.println(deliveryTag+"加到："+atomicInteger);
    //    //    if (atomicInteger.incrementAndGet()<=10){
    //    //        channel.basicNack(deliveryTag, false, true);
    //    //    }else {
    //    //        //十次重试失败后
    //    //        log.error("十次重试后依旧失败: 记录该数据到数据库中"+content);
    //    //        channel.basicNack(deliveryTag, false, false);
    //    //    }
    //    //
    //    //}
    //
    //}
}
