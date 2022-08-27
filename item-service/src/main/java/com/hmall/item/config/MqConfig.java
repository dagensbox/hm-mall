package com.hmall.item.config;

import com.hmall.common.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 12141
 */
@Configuration
public class MqConfig {

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(MqConstants.MALL_EXCHANGE, true, false);
    }

    @Bean
    public Queue insertQueue() {
        return new Queue(MqConstants.MALL_INSERT_QUEUE, true);
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(MqConstants.MALL_DELETE_QUEUE, true);
    }

    @Bean
    public Queue updownQueue() {
        return new Queue(MqConstants.MALL_UPDOWN_QUEUE, true);
    }

    @Bean
    public Binding insertQueueBinding() {
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MqConstants.MALL_INSERT_KEY);
    }

    @Bean
    public Binding deleteQueueBinding() {
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MqConstants.MALL_DELETE_KEY);
    }

    @Bean
    public Binding updownQueueBinding() {
        return BindingBuilder.bind(updownQueue()).to(topicExchange()).with(MqConstants.MALL_UPDOWN_KEY);
    }
}
