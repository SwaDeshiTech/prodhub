package com.swadeshitech.prodhub.services.impl;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.integration.cicaptain.config.CiCaptainClient;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerRequest;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
import com.swadeshitech.prodhub.utils.Base64Util;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.swadeshitech.prodhub.config.ContextHolder;
import com.swadeshitech.prodhub.dto.ReleaseCandidateRequest;
import com.swadeshitech.prodhub.dto.ReleaseCandidateResponse;
import com.swadeshitech.prodhub.entity.ReleaseCandidate;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.ReleaseCandidateService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.swing.text.html.Option;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReleaseCandidateServiceImpl implements ReleaseCandidateService {

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private CiCaptainClient ciCaptainClient;

    private final ObjectMapper objectMapper;

    @Override
    public ReleaseCandidateResponse createReleaseCandidate(ReleaseCandidateRequest request) {

        log.info("Creating release candidate for service: {} with build profile: {}", request.getServiceName(),
                request.getBuildProfile());

        User user = extractUserFromContext();

        List<Application> applications = readTransactionService.findApplicationByFilters(Map.of("_id", new ObjectId(request.getServiceName())));
        if(CollectionUtils.isEmpty(applications)) {
            log.error("Failed to create release candidate, service not found {}", request.getServiceName());
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_COULD_NOT_BE_CREATED);
        }

        ReleaseCandidate releaseCandidate = new ReleaseCandidate();
        releaseCandidate.setBuildProfile(request.getBuildProfile());
        releaseCandidate.setStatus(ReleaseCandidateStatus.CREATED);
        releaseCandidate.setInitiatedBy(user);
        releaseCandidate.setMetaData(request.getMetadata());
        releaseCandidate.setService(applications.getFirst());

        releaseCandidate = writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);

        triggerBuild(releaseCandidate);

        return buildResponse(releaseCandidate);
    }

    @Override
    public ReleaseCandidateResponse getReleaseCandidateById(String id) {

        if (ObjectUtils.isEmpty(id)) {
            log.error("Release candidate ID is null or empty");
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        log.info("Fetching release candidate with ID: {}", id);
        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", new ObjectId(id));

        List<ReleaseCandidate> releaseCandidate = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidate)) {
            log.warn("Release candidate with ID: {} not found", id);
            return null; // or throw an exception
        }

        return buildResponse(releaseCandidate.get(0));
    }

    @Override
    public ReleaseCandidateResponse updateReleaseCandidate(String id, ReleaseCandidateRequest request) {

        log.info("Updating release candidate with ID: {}", id);

        if (ObjectUtils.isEmpty(id)) {
            log.error("Release candidate ID is null or empty");
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        log.info("Fetching release candidate with ID: {}", id);
        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", new ObjectId(id));

        List<ReleaseCandidate> releaseCandidate = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidate)) {
            log.warn("Release candidate with ID: {} not found", id);
            return null; // or throw an exception
        }

        ReleaseCandidate existingReleaseCandidate = releaseCandidate.get(0);

        log.info("Updating release candidate with ID: {}", id);
        existingReleaseCandidate.setStatus(ReleaseCandidateStatus.valueOf(request.getReleaseCandidateStatus()));
        existingReleaseCandidate.setMetaData(request.getMetadata());

        writeTransactionService.saveReleaseCandidateToRepository(existingReleaseCandidate);

        return buildResponse(existingReleaseCandidate);
    }

    @Override
    public List<ReleaseCandidateResponse> getAllReleaseCandidates() {

        log.info("Fetching all release candidates");
        User user = extractUserFromContext();

        Map<String, Object> filters = new HashMap<>();
        filters.put("initiatedBy", user);
        filters.put("status", ReleaseCandidateStatus.CREATED);

        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.warn("No release candidates found");
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        log.info("Found {} release candidates", releaseCandidates.size());
        return releaseCandidates.stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    public void deleteReleaseCandidate(String id) {
        writeTransactionService.removeReleaseCandidateFromRepository(id);
        log.info("Deleted release candidate with ID: {}", id);
    }

    private ReleaseCandidateResponse buildResponse(ReleaseCandidate releaseCandidate) {

        String certifiedBy = "N/A";
        if (!ObjectUtils.isEmpty(releaseCandidate.getCertifiedBy())) {
            User certifiedUser = releaseCandidate.getCertifiedBy();
            certifiedBy = certifiedUser.getName() + " (" + certifiedUser.getEmailId() + ")";
        }

        String initiatedBy = "N/A";
        if (!ObjectUtils.isEmpty(releaseCandidate.getInitiatedBy())) {
            User initiatedUser = releaseCandidate.getInitiatedBy();
            initiatedBy = initiatedUser.getName() + " (" + initiatedUser.getEmailId() + ")";
        }

        return getReleaseCandidateResponse(releaseCandidate, certifiedBy, initiatedBy);
    }

    private static ReleaseCandidateResponse getReleaseCandidateResponse(ReleaseCandidate releaseCandidate, String certifiedBy, String initiatedBy) {
        ReleaseCandidateResponse response = new ReleaseCandidateResponse();
        response.setId(releaseCandidate.getId());
        response.setServiceName(releaseCandidate.getService().getName());
        response.setBuildProfile(releaseCandidate.getBuildProfile());
        response.setStatus(releaseCandidate.getStatus());
        response.setCertifiedBy(certifiedBy);
        response.setInitiatedBy(initiatedBy);
        response.setMetaData(releaseCandidate.getMetaData());

        response.setCreatedBy(releaseCandidate.getCreatedBy());
        response.setCreatedTime(releaseCandidate.getCreatedTime());
        response.setLastModifiedTime(releaseCandidate.getLastModifiedTime());
        response.setLastModifiedBy(releaseCandidate.getLastModifiedBy());
        return response;
    }

    private User extractUserFromContext() {
        String uuid = (String) ContextHolder.getContext("uuid");
        if (ObjectUtils.isEmpty(uuid)) {
            log.error("UUID is null or empty");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Map<String, Object> userFilters = new HashMap<>();
        userFilters.put("uuid", uuid);

        List<User> userOption = readTransactionService.findUserDetailsByFilters(userFilters);
        if (CollectionUtils.isEmpty(userOption)) {
            log.error("User with UUID: {} not found", uuid);
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return userOption.getFirst();
    }

    private void triggerBuild(ReleaseCandidate releaseCandidate) {

        String providerId = "";
        String jobName = releaseCandidate.getService().getName() + "-" + releaseCandidate.getBuildProfile();
        JsonNode data = null;

        if(StringUtils.hasText(releaseCandidate.getEphemeralEnvironmentName())) {
            jobName += "-" + releaseCandidate.getEphemeralEnvironmentName();
        } else {
            Set<Metadata> metadataList = releaseCandidate.getService().getProfiles();
            for(Metadata metadata : metadataList) {
                if(metadata.getName().equals(releaseCandidate.getBuildProfile())) {
                    try {
                        String decodedData = Base64Util.convertToPlainText(metadata.getData());
                        data = objectMapper.readTree(decodedData);
                        providerId = data.path("buildProviderId").asText();
                    } catch (JsonProcessingException e) {
                        log.error("Fail to read metadata of profile {}", metadata.getName());
                        throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
                    }
                    break;
                }
            }
        }

        BuildTriggerRequest request = BuildTriggerRequest.builder()
                .triggeredBy(releaseCandidate.getInitiatedBy().getName() + " (" + releaseCandidate.getInitiatedBy().getEmailId() + ")")
                .parameters(Map.of(
                        "BRANCH_NAME", data.path("branchName").asText(),
                        "BASE_IMAGE", data.path("baseImage").asText(),
                        "BUILD_COMMAND", data.path("buildCommand").asText(),
                        "REPO_URL", "https://github.com/SwaDeshiTech/" + data.path("repo").asText(),
                        "ARTIFACT_PATH", data.path("artifactPath").asText(),
                        "JOB_TEMPLATE", "prodhub_build"
                ))
                .build();

        Mono<BuildTriggerResponse> buildTriggerResponse = ciCaptainClient.triggerBuild(providerId, jobName, request);
        BuildTriggerResponse response = buildTriggerResponse.blockOptional().get();
        releaseCandidate.setStatus(ReleaseCandidateStatus.IN_PROGRESS);
        releaseCandidate.setBuildRefId(response.data().buildId());

        writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);
    }
}
