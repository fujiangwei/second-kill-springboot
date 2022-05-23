package com.kinson.secondkill.mq;

import com.kinson.secondkill.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author
 * @Describe MQ发送类
 * @date
 */
@Component
@Slf4j(topic = "MQReceiver")
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 秒杀消息发送
     *
     * @param message
     */
    public void sendSecKillMessage(String message) {
        log.info("发送消息" + message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, "secKill.message", message);
    }
}
