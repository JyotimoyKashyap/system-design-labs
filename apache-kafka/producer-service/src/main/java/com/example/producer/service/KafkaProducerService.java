package com.example.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "user-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AtomicInteger counter = new AtomicInteger(0);

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 3000)
    public void produceMessagePeriodically() {
        String message = "Automated Message #" + counter.incrementAndGet();
        logger.info(String.format("Producing message -> %s", message));
        this.kafkaTemplate.send(TOPIC, message);
    }
}
