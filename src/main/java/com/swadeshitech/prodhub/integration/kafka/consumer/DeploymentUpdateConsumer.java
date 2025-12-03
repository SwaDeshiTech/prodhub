package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.DeploymentUpdateKafka;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.DeploymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeploymentUpdateConsumer {

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topic.deploymentUpdates}", groupId = "default_group")
    public void listen(String message) {

        log.info("{}: Request for deployment config and submit {}", this.getClass().getCanonicalName(), message);

        try {
            DeploymentUpdateKafka deploymentUpdateKafka = objectMapper.readValue(message, DeploymentUpdateKafka.class);
            deploymentService.updateDeploymentStepStatus(deploymentUpdateKafka);
        } catch (JsonProcessingException e) {
            log.error("{}: Fail to parse the kafka message", this.getClass().getCanonicalName(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
