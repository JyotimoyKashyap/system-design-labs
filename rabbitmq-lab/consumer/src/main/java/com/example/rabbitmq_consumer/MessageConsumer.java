package com.example.rabbitmq_consumer;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {
    
    private final String instanceId = UUID.randomUUID().toString().substring(0, 5);

    @RabbitListener(queues = "demo_queue")
    public void receiveMessage(String message) {
        System.out.println("[Consumer ID : " + instanceId + "] Received Message : " + message);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
