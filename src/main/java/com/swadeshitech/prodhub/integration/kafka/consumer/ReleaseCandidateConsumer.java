package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.ReleaseCandidate;
import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReleaseCandidateConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @KafkaListener(topics = "${spring.kafka.topic.releaseCandidate}", groupId = "default_group")
    public void listen(String message) {
        log.info("Message received for release candidate creation: {}", message);
        
        ReleaseCandidateEvent releaseCandidateEvent;
        try {
            releaseCandidateEvent = objectMapper.readValue(message, ReleaseCandidateEvent.class);
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse release candidate event payload: {}", message, ex);
            return;
        }

        // Validate the event
        if (!StringUtils.hasText(releaseCandidateEvent.buildId())) {
            log.warn("Ignoring release candidate event without build_id");
            return;
        }

        if (!"SUCCESS".equalsIgnoreCase(releaseCandidateEvent.status())) {
            log.info("Build status is not SUCCESS, skipping release candidate creation for build: {}", 
                    releaseCandidateEvent.buildId());
            return;
        }

        // Extract parameters from the event
        ReleaseCandidateEventData eventData = releaseCandidateEvent.data();
        if (eventData == null || eventData.parameters() == null) {
            log.warn("Release candidate event missing data or parameters for build: {}", 
                    releaseCandidateEvent.buildId());
            return;
        }

        // Extract service name from parameters
        String serviceName = extractParameterValue(eventData.parameters(), "SERVICE_NAME");
        if (!StringUtils.hasText(serviceName)) {
            log.warn("SERVICE_NAME not found in parameters for build: {}", releaseCandidateEvent.buildId());
            return;
        }

        // Find the application/service
        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("name", serviceName)
        );
        if (CollectionUtils.isEmpty(applications)) {
            log.error("Application not found for service name: {}", serviceName);
            return;
        }

        Application application = applications.getFirst();

        // Find build profile - this might need to be derived from parameters or metadata
        // For now, we'll try to find a build profile that matches the service
        List<Metadata> buildProfiles = readTransactionService.findByDynamicOrFilters(
                Map.of("serviceId", application.getId()),
                Metadata.class
        );
        
        Metadata buildProfile = null;
        if (!CollectionUtils.isEmpty(buildProfiles)) {
            // Use the first build profile found for this service
            // In a real scenario, you might want to match based on specific criteria
            buildProfile = buildProfiles.getFirst();
        }

        // Create metadata map for the release candidate
        Map<String, String> metadata = new HashMap<>();
        metadata.put("buildRefId", releaseCandidateEvent.buildId());
        metadata.put("providerID", eventData.providerId());
        metadata.put("jobName", eventData.jobName());
        metadata.put("system", eventData.system());
        
        // Extract additional parameters
        metadata.put("repoURL", extractParameterValue(eventData.parameters(), "REPO_URL"));
        metadata.put("branchName", extractParameterValue(eventData.parameters(), "BRANCH_NAME"));
        metadata.put("commitId", extractParameterValue(eventData.parameters(), "COMMIT_ID"));
        metadata.put("buildCommand", extractParameterValue(eventData.parameters(), "BUILD_COMMAND"));
        metadata.put("artifactPath", extractParameterValue(eventData.parameters(), "ARTIFACT_PATH"));
        
        if (StringUtils.hasText(eventData.url())) {
            metadata.put("buildUrl", eventData.url());
        }

        // Create the release candidate
        ReleaseCandidate releaseCandidate = ReleaseCandidate.builder()
                .service(application)
                .buildProfile(buildProfile)
                .status(ReleaseCandidateStatus.CREATED)
                .metaData(metadata)
                .createdBy("ci-captain")
                .createdTime(LocalDateTime.now())
                .lastModifiedBy("ci-captain")
                .lastModifiedTime(LocalDateTime.now())
                .build();

        // Save the release candidate
        try {
            writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);
            log.info("Successfully created release candidate for build: {} and service: {}", 
                    releaseCandidateEvent.buildId(), serviceName);
        } catch (Exception ex) {
            log.error("Failed to save release candidate for build: {}", releaseCandidateEvent.buildId(), ex);
        }
    }

    private String extractParameterValue(Map<String, Object> parameters, String key) {
        if (parameters == null || !StringUtils.hasText(key)) {
            return null;
        }
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReleaseCandidateEvent(
            @JsonProperty("event") String event,
            @JsonProperty("build_id") String buildId,
            @JsonProperty("status") String status,
            @JsonProperty("job_name") String jobName,
            @JsonProperty("system") String system,
            @JsonProperty("timestamp") String timestamp,
            @JsonProperty("source") String source,
            @JsonProperty("data") ReleaseCandidateEventData data
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReleaseCandidateEventData(
            @JsonProperty("build_id") String buildId,
            @JsonProperty("ref_id") String refId,
            @JsonProperty("job_name") String jobName,
            @JsonProperty("status") String status,
            @JsonProperty("provider_id") String providerId,
            @JsonProperty("triggered_by") String triggeredBy,
            @JsonProperty("start_time") String startTime,
            @JsonProperty("finish_time") String finishTime,
            @JsonProperty("url") String url,
            @JsonProperty("parameters") Map<String, Object> parameters
    ) {}
}
