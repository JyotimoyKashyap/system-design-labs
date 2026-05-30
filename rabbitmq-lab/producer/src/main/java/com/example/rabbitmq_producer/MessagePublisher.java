package com.example.rabbitmq_producer;

import java.time.LocalTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;

    public MessagePublisher (RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedRate = 2000)
    public void sendMessage() {
        String message = "Hello RabbitMQ! Time is : " + LocalTime.now();
        System.out.println("Producer sending : " + message);

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
    }
}
