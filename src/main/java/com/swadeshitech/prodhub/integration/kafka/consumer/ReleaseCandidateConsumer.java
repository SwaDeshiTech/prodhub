package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.EphemeralEnvironment;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.entity.ReleaseCandidate;
import com.swadeshitech.prodhub.entity.Template;
import com.swadeshitech.prodhub.entity.User;
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
        if (eventData == null) {
            log.warn("Release candidate event missing data for build: {}", 
                    releaseCandidateEvent.buildId());
            return;
        }

        // Extract service name from parameters or use job_name as fallback
        String serviceName = null;
        if (eventData.parameters() != null) {
            serviceName = extractParameterValue(eventData.parameters(), "SERVICE_NAME");
        }
        if (!StringUtils.hasText(serviceName)) {
            // Fallback to job_name if SERVICE_NAME not in parameters
            serviceName = eventData.jobName();
        }
        if (!StringUtils.hasText(serviceName)) {
            log.warn("Service name not found in parameters or job_name for build: {}", releaseCandidateEvent.buildId());
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

        // Find pipeline execution by ciCaptainBuildId
        PipelineExecution pipelineExecution = null;
        List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                Map.of("metaData.ciCaptainBuildId", releaseCandidateEvent.buildId()),
                PipelineExecution.class
        );
        if (!CollectionUtils.isEmpty(pipelineExecutions)) {
            pipelineExecution = pipelineExecutions.getFirst();
        }

        // Find build profile - use the metaDataId from pipeline execution metadata
        Metadata buildProfile = null;
        if (pipelineExecution != null && pipelineExecution.getMetaData() != null) {
            String metaDataId = (String) pipelineExecution.getMetaData().get("metaDataId");
            if (StringUtils.hasText(metaDataId)) {
                List<Metadata> buildProfiles = readTransactionService.findMetaDataByFilters(
                        Map.of("_id", new ObjectId(metaDataId)));
                if (!CollectionUtils.isEmpty(buildProfiles)) {
                    buildProfile = buildProfiles.getFirst();
                }
            }
        }
        
        // Fallback: try to find build profile by serviceId if not found via metaDataId
        if (buildProfile == null) {
            List<Metadata> buildProfiles = readTransactionService.findByDynamicOrFilters(
                    Map.of("serviceId", application.getId()),
                    Metadata.class
            );
            if (!CollectionUtils.isEmpty(buildProfiles)) {
                buildProfile = buildProfiles.getFirst();
            }
        }
        
        if (buildProfile == null) {
            log.error("Build profile not found for service: {}", serviceName);
            return;
        }

        // Find ephemeral environment (optional - may not always be present)
        EphemeralEnvironment ephemeralEnvironment = null;
        if (pipelineExecution != null && pipelineExecution.getMetaData() != null) {
            String ephemeralEnvId = (String) pipelineExecution.getMetaData().get("ephemeralEnvironmentId");
            if (StringUtils.hasText(ephemeralEnvId)) {
                List<EphemeralEnvironment> ephemeralEnvironments = readTransactionService.findByDynamicOrFilters(
                        Map.of("_id", new ObjectId(ephemeralEnvId)),
                        EphemeralEnvironment.class
                );
                if (!CollectionUtils.isEmpty(ephemeralEnvironments)) {
                    ephemeralEnvironment = ephemeralEnvironments.getFirst();
                }
            }
        }

        // Find user who initiated the build (from triggered_by field)
        User initiatedBy = null;
        if (StringUtils.hasText(eventData.triggeredBy())) {
            List<User> users = readTransactionService.findByDynamicOrFilters(
                    Map.of("email", eventData.triggeredBy()),
                    User.class
            );
            if (!CollectionUtils.isEmpty(users)) {
                initiatedBy = users.getFirst();
            }
        }

        // Create metadata map for the release candidate
        Map<String, String> metadata = new HashMap<>();
        metadata.put("buildRefId", releaseCandidateEvent.buildId());
        metadata.put("providerID", eventData.providerId());
        metadata.put("jobName", eventData.jobName());
        metadata.put("system", releaseCandidateEvent.system());
        
        // Extract additional parameters from step metadata if event parameters are null
        if (eventData.parameters() != null) {
            metadata.put("repoURL", extractParameterValue(eventData.parameters(), "REPO_URL"));
            metadata.put("branchName", extractParameterValue(eventData.parameters(), "BRANCH_NAME"));
            metadata.put("commitId", extractParameterValue(eventData.parameters(), "COMMIT_ID"));
            metadata.put("dockerImageHashValue", extractParameterValue(eventData.parameters(), "DOCKER_IMAGE_HASH_VALUE"));
            metadata.put("buildCommand", extractParameterValue(eventData.parameters(), "BUILD_COMMAND"));
            metadata.put("artifactPath", extractParameterValue(eventData.parameters(), "ARTIFACT_PATH"));
        } else if (pipelineExecution != null && pipelineExecution.getStageExecutions() != null) {
            // Try to extract from pipeline execution step metadata
            for (PipelineExecution.StageExecution stage : pipelineExecution.getStageExecutions()) {
                if ("build".equalsIgnoreCase(stage.getStageName()) && stage.getTemplate() != null 
                        && !CollectionUtils.isEmpty(stage.getTemplate().getSteps())) {
                    for (Template.Step step : stage.getTemplate().getSteps()) {
                        if ("build".equalsIgnoreCase(step.getStepName()) && step.getParams() != null) {
                            // Extract values from step params
                            metadata.put("repoURL", extractParamValue(step.getParams(), "repoURL"));
                            metadata.put("branchName", extractParamValue(step.getParams(), "branchName"));
                            metadata.put("commitId", extractParamValue(step.getParams(), "commitId"));
                            metadata.put("dockerImageHashValue", extractParamValue(step.getParams(), "dockerImageHashValue"));
                            metadata.put("buildCommand", extractParamValue(step.getParams(), "buildCommand"));
                            metadata.put("artifactPath", extractParamValue(step.getParams(), "artifactPath"));
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        if (StringUtils.hasText(eventData.url())) {
            metadata.put("buildUrl", eventData.url());
        }

        // Create the release candidate
        ReleaseCandidate releaseCandidate = ReleaseCandidate.builder()
                .service(application)
                .buildProfile(buildProfile)
                .status(ReleaseCandidateStatus.CERTIFIABLE)
                .metaData(metadata)
                .buildRefId(releaseCandidateEvent.buildId())
                .ephemeralEnvironment(ephemeralEnvironment)
                .initiatedBy(initiatedBy)
                .pipelineExecution(pipelineExecution)
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

    private String extractParamValue(Map<String, Template.Step.TemplateStepParam> params, String key) {
        if (params == null || !StringUtils.hasText(key)) {
            return null;
        }
        // First try direct map key lookup (e.g., "repoURL", "commitId")
        Template.Step.TemplateStepParam directMatch = params.get(key);
        if (directMatch != null && StringUtils.hasText(directMatch.getValue())) {
            return directMatch.getValue();
        }
        // Fallback: search by displayName or affectedKey
        for (Template.Step.TemplateStepParam param : params.values()) {
            if (key.equalsIgnoreCase(param.getDisplayName()) || key.equalsIgnoreCase(param.getAffectedKey())) {
                return param.getValue();
            }
        }
        return null;
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
