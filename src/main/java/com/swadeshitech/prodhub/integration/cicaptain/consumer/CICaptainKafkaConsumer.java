package com.swadeshitech.prodhub.integration.cicaptain.consumer;

import com.swadeshitech.prodhub.entity.Deployment;
import com.swadeshitech.prodhub.entity.EphemeralEnvironment;
import com.swadeshitech.prodhub.entity.ReleaseCandidate;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.DeploymentService;
import com.swadeshitech.prodhub.services.ReleaseCandidateService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @KafkaListener(topics = "${spring.kafka.topic.buildStatus}", groupId = "default_group")
    public void listen(String message) {
        log.info("{}: Syncing build status {}", this.getClass().getCanonicalName(), message);

        ReleaseCandidate releaseCandidate = fetchReleaseCandidate(message);

        releaseCandidateService.syncStatus(releaseCandidate.getId(), "true");
    }

    @KafkaListener(topics = "${spring.kafka.topic.buildStatusComplete}", groupId = "default_group")
    public void listenBuildComplete(String message) {
        log.info("{}: Complete build status {}", this.getClass().getCanonicalName(), message);
        triggerDeployment(message);
    }

    private void triggerDeployment(String buildRefId) {
        log.info("Starting triggering the deployment for buildProfile with refId {}", buildRefId);
        ReleaseCandidate releaseCandidate = fetchReleaseCandidate(buildRefId);
        if(Objects.nonNull(releaseCandidate.getEphemeralEnvironment())) {
            for(EphemeralEnvironment.Profile profile : releaseCandidate.getEphemeralEnvironment().getAttachedProfiles()) {
                if(profile.getBuildProfile().equals(releaseCandidate.getBuildProfile())) {
                    log.info("Triggering the deployment for ephemeral environment {} for deployment profile {}",
                            releaseCandidate.getEphemeralEnvironment().getName(), profile.getDeploymentProfile().getName());
                    Deployment deployment = deploymentService.triggerDeploymentForEphemeralEnvironment(releaseCandidate.getEphemeralEnvironment(),
                            profile.getDeploymentProfile(), releaseCandidate);
                    deploymentService.submitDeploymentRequest(deployment.getId());
                    return;
                }
            }
        }
    }

    private ReleaseCandidate fetchReleaseCandidate(String buildRefId){
        Map<String, Object> filters = new HashMap<>();
        filters.put("buildRefId", buildRefId);
        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.error("Failed to fetch the release candidate for buildRefId {}", buildRefId);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        return releaseCandidates.getFirst();
    }
}
