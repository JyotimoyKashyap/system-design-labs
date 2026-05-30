package com.example.rabbitmq_consumer;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue myQueue() {
        // This ensures the consumer will create the queue if the producer hasn't yet!
        return new Queue("demo_queue", true);
    }
}
