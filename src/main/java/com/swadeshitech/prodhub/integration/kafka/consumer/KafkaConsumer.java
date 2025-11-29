package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.swadeshitech.prodhub.services.DeploymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class KafkaConsumer {

    @Autowired
    DeploymentService deploymentService;

    @KafkaListener(topics = "${spring.kafka.topic.deploymentConfigAndSubmit}", groupId = "default_group")
    public void listen(String message) {

        log.info("{}: Request for deployment config and submit {}", this.getClass().getCanonicalName(), message);

        deploymentService.generateDeploymentConfig(message);

        deploymentService.submitDeploymentRequest(message);
    }
}
