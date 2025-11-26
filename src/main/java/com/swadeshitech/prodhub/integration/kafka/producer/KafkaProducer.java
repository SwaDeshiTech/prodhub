package com.swadeshitech.prodhub.integration.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Generic send method
    public void sendMessage(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
    }

    // Overload without key if not needed
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}

