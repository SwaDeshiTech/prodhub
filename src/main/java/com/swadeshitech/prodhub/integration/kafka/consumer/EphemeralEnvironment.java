package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.config.ContextHolder;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentBuildAndDeployRequest;
import com.swadeshitech.prodhub.dto.ReleaseCandidateRequest;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.services.ReleaseCandidateService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EphemeralEnvironment {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    ReleaseCandidateService releaseCandidateService;

    @KafkaListener(topics = "${spring.kafka.topic.ephemeralEnvironmentUpdate}", groupId = "default_group")
    public void listen(String message) {

    }

    @KafkaListener(topics = "${spring.kafka.topic.ephemeralEnvironmentBuildAndDeployment}", groupId = "default_group")
    public void listenBuildAndDeploymentRequest(String message) {
        log.info("{} Message received for buildAndDeploy in ephemeral environment {}", this.getClass().getCanonicalName(), message);
        try {
            EphemeralEnvironmentBuildAndDeployRequest request = objectMapper.readValue(message, EphemeralEnvironmentBuildAndDeployRequest.class);
            log.info("Processing request for buildAndDeploy for environment {}", request.getEphemeralEnvironmentId());

            if (request.getUserId() != null) {
                ContextHolder.setContext("uuid", request.getUserId());
            } else {
                log.warn("UUID missing in Kafka message, extractUserFromContext might fail");
            }

            List<com.swadeshitech.prodhub.entity.EphemeralEnvironment> ephemeralEnvironments = readTransactionService.findByDynamicOrFilters(
                    Map.of("_id", new ObjectId(request.getEphemeralEnvironmentId())), com.swadeshitech.prodhub.entity.EphemeralEnvironment.class);
            if(CollectionUtils.isEmpty(ephemeralEnvironments)) {
                log.error("Ephemeral environment could not be found {}", request.getEphemeralEnvironmentId());
                return;
            }
            com.swadeshitech.prodhub.entity.EphemeralEnvironment ephemeralEnvironment = ephemeralEnvironments.getFirst();
            for (EphemeralEnvironmentBuildAndDeployRequest.EphemeralEnvironmentServiceProfiles profile : request.getProfiles()) {
                List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(Map.of("_id", new ObjectId(profile.getBuildProfileId())));
                if (CollectionUtils.isEmpty(metadataList)) {
                    log.error("Metadata could not be found {}", profile.getBuildProfileId());
                    continue;
                }
                Metadata metadata = metadataList.getFirst();
                JsonNode data = objectMapper.readTree(Base64Util.convertToPlainText(metadata.getData()));
                releaseCandidateService.createReleaseCandidate(ReleaseCandidateRequest.builder()
                                .buildProfile(metadata.getId())
                                .ephemeralEnvironmentName(ephemeralEnvironment.getId())
                                .serviceName(metadata.getApplication().getId())
                                .metadata(Map.of("branchName", data.path("branchName").asText(),
                                        "commitId", data.path("commitId").asText()))
                        .build());
            }
        } catch (JsonProcessingException e) {
            log.error("{} Failed to parse the message", this.getClass().getCanonicalName(), e);
        } catch (Exception e) {
            log.error("Error during processing Kafka message", e);
        } finally {
            ContextHolder.clearContext();
        }
    }
}
