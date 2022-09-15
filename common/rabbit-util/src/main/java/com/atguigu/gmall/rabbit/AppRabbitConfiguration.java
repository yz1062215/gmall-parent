package com.atguigu.gmall.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRabbit //开启rabbitmq注解支持
@Slf4j
public class AppRabbitConfiguration {


    @Bean
    RabbitTemplate rabbitTemplate(RabbitTemplateConfigurer configurer, ConnectionFactory connectionFactory){

        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        configurer.configure(rabbitTemplate, connectionFactory);

        //感知消息是否被投递到队列  投递失败触发回调机制
        rabbitTemplate.setReturnCallback((Message message,
                                          int replyCode,
                                          String replyText,
                                          String exchange,
                                          String routeKey) -> {
            log.error("消息投递到队列失败，保存到数据库,{}",message);
        });
        //感知消息是否被真正投递到mq服务器  重试
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData,
                                           boolean ack,
                                           String cause)->{
            if (ack){
                //交换机收到消息
                System.out.println("交换机收到消息");
                log.error("消息投递到服务器失败，保存到数据库， 消息：{}",correlationData);
            }

        });

        rabbitTemplate.setRetryTemplate(new RetryTemplate());//设置重试器


        return rabbitTemplate;

    }
}
