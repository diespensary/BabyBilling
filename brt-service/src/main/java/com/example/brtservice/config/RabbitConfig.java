package com.example.brtservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    //
    // CDR → BRT
    //
    @Bean
    public Queue cdrQueue() {
        return new Queue("cdr.queue");
    }

    //
    // BRT → HRS
    //
    @Bean
    public DirectExchange hrsExchange() {
        return new DirectExchange("hrs.exchange");
    }

    @Bean
    public Queue hrsQueue() {
        return new Queue("hrs.queue");
    }

    @Bean
    public Binding hrsBinding(Queue hrsQueue, DirectExchange hrsExchange) {
        return BindingBuilder
                .bind(hrsQueue)
                .to(hrsExchange)
                .with("hrs.routingKey");
    }
}
