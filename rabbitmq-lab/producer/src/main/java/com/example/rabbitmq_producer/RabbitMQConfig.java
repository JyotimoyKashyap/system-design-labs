package com.example.rabbitmq_producer;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;

public class RabbitMQConfig {
    public static final String QUEUE_NAME = "demo_queue";

    @Bean
    public Queue myQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}
