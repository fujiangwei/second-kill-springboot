package com.kinson.secondkill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author
 * @Describe MQ配置类
 * @date
 */
@Configuration
public class RabbitMQConfig {
    /**
     * 队列名
     */
    private static final String QUEUE_NAME = "secKillQueue";
    /**
     * 交换机名
     */
    public static final String TOPIC_EXCHANGE_NAME = "secKillTopicExchange";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(topicExchange()).with("secKill.#");
    }
}
