package com.swadeshitech.prodhub.integration.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BuildConsumer {

    @KafkaListener(topics = "${spring.kafka.topic.buildUpdate}", groupId = "default_group")
    public void listen(String message) {
        log.info("Message received for buildUpdates for rc {}", message);


    }
}
