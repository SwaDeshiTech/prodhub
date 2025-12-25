package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.swadeshitech.prodhub.services.DeploymentSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeploymentSetConsumer {

    @Autowired
    DeploymentSetService deploymentSetService;

    @KafkaListener(topics = "${spring.kafka.topic.deploymentSetStatusUpdate}", groupId = "default_group")
    public void listen(String message) {

        log.info("{}: Request for deployment set update {}", this.getClass().getCanonicalName(), message);

        deploymentSetService.updateDeploymentSetStatus(message);

        log.info("{}: Request completed for deployment set update {}", this.getClass().getCanonicalName(), message);
    }
}
