package com.example.cdrservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue cdrQueue() {
        return new Queue("cdr.queue");
    }

    @Bean
    public TopicExchange cdrExchange() {
        return new TopicExchange("cdr.exchange");
    }

    @Bean
    public Binding binding(Queue cdrQueue, TopicExchange cdrExchange) {
        return BindingBuilder.bind(cdrQueue)
                .to(cdrExchange)
                .with("cdr.routingKey");
    }
}

