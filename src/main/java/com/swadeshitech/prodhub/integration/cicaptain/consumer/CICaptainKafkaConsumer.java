package com.swadeshitech.prodhub.integration.cicaptain.consumer;

import com.swadeshitech.prodhub.entity.ReleaseCandidate;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.ReleaseCandidateService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CICaptainKafkaConsumer {

    @Autowired
    ReleaseCandidateService releaseCandidateService;

    @Autowired
    ReadTransactionService readTransactionService;

    @KafkaListener(topics = "${spring.kafka.topic.buildStatus}", groupId = "default_group")
    public void listen(String message) {
        log.info("{}: Syncing build status {}", this.getClass().getCanonicalName(), message);

        Map<String, Object> filters = new HashMap<>();
        filters.put("buildRefId", message);

        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.error("Failed to fetch the release candidate for buildRefId {}", message);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        ReleaseCandidate releaseCandidate = releaseCandidates.getFirst();

        releaseCandidateService.syncStatus(releaseCandidate.getId(), "true");
    }
}
