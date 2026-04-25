package com.swadeshitech.prodhub.integration.cicaptain.consumer;

import com.swadeshitech.prodhub.entity.Deployment;
import com.swadeshitech.prodhub.entity.EphemeralEnvironment;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.entity.ReleaseCandidate;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.cicaptain.config.CiCaptainClient;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildStatusResponse;
import com.swadeshitech.prodhub.services.DeploymentService;
import com.swadeshitech.prodhub.services.PipelineService;
import com.swadeshitech.prodhub.services.ReleaseCandidateService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class CICaptainKafkaConsumer {

    @Autowired
    ReleaseCandidateService releaseCandidateService;

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    PipelineService pipelineService;

    @Autowired
    CiCaptainClient ciCaptainClient;

    @Value("${cicaptain.default.provider.id}")
    String defaultProviderId;

    @KafkaListener(topics = "${spring.kafka.topic.buildStatus}", groupId = "default_group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message) {
        log.info("{}: Syncing build status {}", this.getClass().getCanonicalName(), message);

        ReleaseCandidate releaseCandidate = fetchReleaseCandidate(message);
        if (releaseCandidate == null) {
            log.warn("Release candidate not found for build status sync, skipping");
            return;
        }

        releaseCandidateService.syncStatus(releaseCandidate.getId(), "true");
    }

    @KafkaListener(topics = "${spring.kafka.topic.buildStatusComplete}", groupId = "default_group", containerFactory = "kafkaListenerContainerFactory")
    public void listenBuildComplete(String message) {
        log.info("{}: Complete build status {}", this.getClass().getCanonicalName(), message);
        
        // Process pipeline stage progression
        try {
            // Fetch actual build status from ci-captain
            String buildStatus = fetchBuildStatus(message);
            pipelineService.processBuildCompletion(message, buildStatus);
        } catch (Exception e) {
            log.error("Failed to process build completion for pipeline progression", e);
        }
        
        // Trigger deployment for ephemeral environments (existing logic)
        triggerDeployment(message);
    }

    private String fetchBuildStatus(String buildRefId) {
        try {
            // Try to get provider ID from pipeline execution metadata
            List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(
                    Map.of("metaData.ciCaptainBuildId", buildRefId), PipelineExecution.class);
            
            String providerId = defaultProviderId;
            if (!CollectionUtils.isEmpty(pipelineExecutions)) {
                PipelineExecution pipelineExecution = pipelineExecutions.getFirst();
                // Check if provider ID is stored in metadata
                if (pipelineExecution.getMetaData() != null && pipelineExecution.getMetaData().containsKey("ciCaptainProviderId")) {
                    providerId = (String) pipelineExecution.getMetaData().get("ciCaptainProviderId");
                }
            }
            
            // Fetch build status from ci-captain
            BuildStatusResponse buildStatusResponse = ciCaptainClient.getBuildStatus(providerId, buildRefId, "true").block();
            if (buildStatusResponse != null) {
                return buildStatusResponse.status();
            }
        } catch (Exception e) {
            log.error("Failed to fetch build status from ci-captain for buildRefId: {}", buildRefId, e);
        }
        
        // Default to SUCCESS if unable to fetch status
        log.warn("Unable to fetch build status, defaulting to SUCCESS for buildRefId: {}", buildRefId);
        return "SUCCESS";
    }

    private void triggerDeployment(String buildRefId) {
        log.info("Starting triggering the deployment for buildProfile with refId {}", buildRefId);
        ReleaseCandidate releaseCandidate = fetchReleaseCandidate(buildRefId);
        if (releaseCandidate == null) {
            log.warn("Release candidate not found for deployment trigger, skipping");
            return;
        }
        if(Objects.nonNull(releaseCandidate.getEphemeralEnvironmentId())) {
            // Fetch EphemeralEnvironment by ID
            List<EphemeralEnvironment> ephemeralEnvironments = readTransactionService.findByDynamicOrFilters(
                    Map.of("_id", new ObjectId(releaseCandidate.getEphemeralEnvironmentId())), EphemeralEnvironment.class);
            if(CollectionUtils.isEmpty(ephemeralEnvironments)) {
                log.warn("Ephemeral environment not found for ID: {}", releaseCandidate.getEphemeralEnvironmentId());
                return;
            }
            EphemeralEnvironment ephemeralEnvironment = ephemeralEnvironments.getFirst();
            
            for(EphemeralEnvironment.Profile profile : ephemeralEnvironment.getAttachedProfiles()) {
                if(profile.getBuildProfile().equals(releaseCandidate.getBuildProfile())) {
                    log.info("Triggering the deployment for ephemeral environment {} for deployment profile {}",
                            ephemeralEnvironment.getName(), profile.getDeploymentProfile().getName());
                    Deployment deployment = deploymentService.triggerDeploymentForEphemeralEnvironment(ephemeralEnvironment,
                            profile.getDeploymentProfile(), releaseCandidate);
                    deploymentService.submitDeploymentRequest(deployment.getId());
                    return;
                }
            }
        }
    }

    private ReleaseCandidate fetchReleaseCandidate(String buildRefId){
        Map<String, Object> filters = new HashMap<>();
        filters.put("metaData.buildRefId", buildRefId);
        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.warn("Release candidate not found for buildRefId {}", buildRefId);
            return null;
        }

        return releaseCandidates.getFirst();
    }
}
