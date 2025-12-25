package com.swadeshitech.prodhub.integration.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EphemeralEnvironment {

    @KafkaListener(topics = "${spring.kafka.topic.ephemeralEnvironmentUpdate}", groupId = "default_group")
    public void listen(String message) {

    }
}
