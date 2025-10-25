package com.swadeshitech.prodhub.services.impl;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.integration.cicaptain.config.CiCaptainClient;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildStatusResponse;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerRequest;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
import com.swadeshitech.prodhub.services.EphemeralEnvironmentService;
import com.swadeshitech.prodhub.utils.Base64Util;
import com.swadeshitech.prodhub.utils.UuidUtil;
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

    @Autowired
    EphemeralEnvironmentService ephemeralEnvironmentService;

    private final ObjectMapper objectMapper;

    @Autowired
    UserServiceImpl userService;

    @Override
    public ReleaseCandidateResponse createReleaseCandidate(ReleaseCandidateRequest request) {

        log.info("Creating release candidate for service: {} with build profile: {}", request.getServiceName(),
                request.getBuildProfile());

        User user = userService.extractUserFromContext();

        List<Application> applications = readTransactionService
                .findApplicationByFilters(Map.of("_id", new ObjectId(request.getServiceName())));
        if (CollectionUtils.isEmpty(applications)) {
            log.error("Failed to create release candidate, service not found {}", request.getServiceName());
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_COULD_NOT_BE_CREATED);
        }

        ReleaseCandidate releaseCandidate = new ReleaseCandidate();
        releaseCandidate.setBuildProfile(request.getBuildProfile());
        releaseCandidate.setStatus(ReleaseCandidateStatus.CREATED);
        releaseCandidate.setInitiatedBy(user);
        releaseCandidate.setMetaData(request.getMetadata());
        releaseCandidate.setService(applications.getFirst());
        releaseCandidate.setBuildRefId(UuidUtil.generateRandomUuid());

        releaseCandidate = writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);

        triggerBuild(releaseCandidate);

        return buildResponse(releaseCandidate);
    }

    @Override
    public ReleaseCandidateResponse getReleaseCandidateById(String id) {

        if (org.apache.commons.lang3.StringUtils.isBlank(id)) {
            log.error("Release candidate ID is null or empty");
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        log.info("Fetching release candidate with ID: {}", id);
        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", new ObjectId(id));

        List<ReleaseCandidate> releaseCandidate = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidate)) {
            log.warn("Release candidate with ID: {} not found", id);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        return buildResponse(releaseCandidate.getFirst());
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
        User user = userService.extractUserFromContext();

        Map<String, Object> filters = new HashMap<>();
        filters.put("initiatedBy", user);

        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.warn("No release candidates found");
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        releaseCandidates.sort(Comparator.comparing(ReleaseCandidate::getCreatedTime).reversed());

        log.info("Found {} release candidates", releaseCandidates.size());
        return releaseCandidates.stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    public ReleaseCandidateResponse syncStatus(String buildId, String forceSync) {

        if (ObjectUtils.isEmpty(buildId)) {
            log.error("Release candidate ID is null or empty");
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        log.info("Fetching release candidate with ID: {}", buildId);
        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", new ObjectId(buildId));

        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.warn("Release candidate with ID: {} not found", buildId);
            return null; // or throw an exception
        }

        ReleaseCandidate releaseCandidate = releaseCandidates.getFirst();

        String providerID = releaseCandidate.getMetaData().get("providerID");

        Mono<BuildStatusResponse> buildStatusResponseMono = ciCaptainClient.getBuildStatus(providerID,
                releaseCandidate.getBuildRefId(), forceSync);
        BuildStatusResponse response = buildStatusResponseMono.blockOptional().get();

        releaseCandidate.setStatus(mapStatusFromCICaptain(response.status()));

        writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);

        return buildResponse(releaseCandidate);
    }

    @Override
    public List<DropdownDTO> getDropdownCertifiable(String applicationId) {

        List<Application> applications = readTransactionService
                .findApplicationByFilters(Map.of("_id", new ObjectId(applicationId)));
        if (CollectionUtils.isEmpty(applications)) {
            log.error("Failed to create release candidate, service not found {}", applicationId);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_COULD_NOT_BE_CREATED);
        }

        Application application = applications.getFirst();

        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(Map.of("service", application, "status", "CERTIFIED"));
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.error("Release candidates could not be found for application id {}", applicationId);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        List<DropdownDTO> dropdownDTOList = new ArrayList<>();

        for (ReleaseCandidate releaseCandidate : releaseCandidates) {
            String key = "Commit ID: " + releaseCandidate.getMetaData().get("commitId") +
                    " Certified by: " +
                    releaseCandidate.getCertifiedBy().getNameAndEmailId();
            dropdownDTOList.add(DropdownDTO.builder()
                            .key(releaseCandidate.getId())
                            .value(key)
                    .build());
        }

        return dropdownDTOList;
    }

    @Override
    public ReleaseCandidateResponse certifyRelaseCandidateForProduction(String id) {

        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(Map.of(
                "_id", new ObjectId(id),
                "status", ReleaseCandidateStatus.CERTIFIABLE));
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.error("Release candidates could not be found for application id {}", id);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        ReleaseCandidate releaseCandidate = releaseCandidates.getFirst();

        //trigger the pipeline
        User certifiedBy = userService.extractUserFromContext();
        releaseCandidate.setStatus(ReleaseCandidateStatus.CERTIFIED);
        releaseCandidate.setCertifiedBy(certifiedBy);

        writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);

        return buildResponse(releaseCandidate);
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

        List<Metadata> metadataList = readTransactionService
                .findMetaDataByFilters(Map.of("_id", new ObjectId(releaseCandidate.getBuildProfile())));
        if (CollectionUtils.isEmpty(metadataList)) {
            log.error("Metadata could not be found {}", releaseCandidate.getId());
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        Metadata metadata = metadataList.getFirst();

        ReleaseCandidateResponse response = getReleaseCandidateResponse(releaseCandidate, certifiedBy, initiatedBy);
        response.setBuildProfile(metadata.getName());

        return response;
    }

    private static ReleaseCandidateResponse getReleaseCandidateResponse(ReleaseCandidate releaseCandidate,
            String certifiedBy, String initiatedBy) {

        ReleaseCandidateResponse response = new ReleaseCandidateResponse();
        response.setId(releaseCandidate.getId());
        response.setServiceName(releaseCandidate.getService().getName());
        response.setStatus(releaseCandidate.getStatus());
        response.setCertifiedBy(certifiedBy);
        response.setInitiatedBy(initiatedBy);
        response.setMetaData(releaseCandidate.getMetaData());
        response.setBuildRefId(releaseCandidate.getBuildRefId());

        response.setCreatedBy(releaseCandidate.getCreatedBy());
        response.setCreatedTime(releaseCandidate.getCreatedTime());
        response.setLastModifiedTime(releaseCandidate.getLastModifiedTime());
        response.setLastModifiedBy(releaseCandidate.getLastModifiedBy());
        return response;
    }



    private void triggerBuild(ReleaseCandidate releaseCandidate) {

        String providerId = "";
        String jobName = releaseCandidate.getService().getName() + "-" + releaseCandidate.getBuildProfile();
        JsonNode data = null;
        String commitId = releaseCandidate.getMetaData().get("commitId");
        String decodedData = "";

        if (StringUtils.hasText(releaseCandidate.getEphemeralEnvironmentName())) {
            jobName += "-" + releaseCandidate.getEphemeralEnvironmentName();
            Map<String, Object> applications = ephemeralEnvironmentService.getMetadataFromEphemeralEnvironment(releaseCandidate.getEphemeralEnvironmentName());
            for (Map.Entry<String, Object> itr : applications.entrySet()) {
                if (itr.getKey().equals(releaseCandidate.getService().getId())) {
                    Map<String, String> profiles = (Map<String, String>) itr.getValue();
                    for(Map.Entry<String, String> profile : profiles.entrySet()) {
                        if (profile.getKey().equals(releaseCandidate.getBuildProfile())) {
                            decodedData = Base64Util.convertToPlainText(profile.getValue());
                            try {
                                data = objectMapper.readTree(decodedData);
                                providerId = data.path("buildProviderId").asText();
                            } catch (JsonProcessingException e) {
                                log.error("Fail to read metadata of profile {} in ephemeral environment {}", releaseCandidate.getBuildProfile(), releaseCandidate.getEphemeralEnvironmentName());
                                throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } else {
            Set<Metadata> metadataList = releaseCandidate.getService().getProfiles();
            for (Metadata metadata : metadataList) {
                if (metadata.getId().equals(releaseCandidate.getBuildProfile())) {
                    try {
                        decodedData = Base64Util.convertToPlainText(metadata.getData());
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

        String dockerImageHashValue = releaseCandidate.getService().getName().toLowerCase() + "-"
                + Base64Util.generate7DigitHash(decodedData) + ":" + commitId.substring(0, 7);

        Map<String, String> params = new HashMap<>();
        params.put("BRANCH_NAME", data.path("branchName").asText());
        params.put("COMMIT_ID", commitId);
        params.put("BASE_IMAGE", data.path("baseImage").asText());
        params.put("BUILD_COMMAND", data.path("buildCommand").asText());
        params.put("REPO_URL", "https://github.com/SwaDeshiTech/" + data.path("repo").asText());
        params.put("ARTIFACT_PATH", data.path("artifactPath").asText());
        params.put("JOB_TEMPLATE", "prodhub_build");
        params.put("SERVICE_NAME", releaseCandidate.getService().getName());
        params.put("PROFILE_PATH", data.path("profilePath").asText());
        params.put("DOCKER_IMAGE_HASH_VALUE", dockerImageHashValue);
        params.put("BUILD_PATH", data.path("buildPath").asText());

        BuildTriggerRequest request = BuildTriggerRequest.builder()
                .triggeredBy(releaseCandidate.getInitiatedBy().getName() + " ("
                        + releaseCandidate.getInitiatedBy().getEmailId() + ")")
                .parameters(params)
                .refId(releaseCandidate.getBuildRefId())
                .build();

        Mono<BuildTriggerResponse> buildTriggerResponse = ciCaptainClient.triggerBuild(providerId, jobName, request);
        BuildTriggerResponse response = buildTriggerResponse.blockOptional().get();
        log.info("Printing ci-captain build response {}", response);
        releaseCandidate.setStatus(ReleaseCandidateStatus.IN_PROGRESS);
        releaseCandidate.getMetaData().put("providerID", providerId);
        releaseCandidate.getMetaData().put("dockerImageHashValue", dockerImageHashValue);

        writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);
    }

    private ReleaseCandidateStatus mapStatusFromCICaptain(String status) {
        switch (status) {
            case "SUCCESS":
                return ReleaseCandidateStatus.CERTIFIABLE;
            case "FAILURE":
            case "FAILED":
                return ReleaseCandidateStatus.FAILED;
            case "ABORTED":
            case "CANCELLED":
                return ReleaseCandidateStatus.CANCELLED;
        }
        return ReleaseCandidateStatus.REJECTED;
    }
}
