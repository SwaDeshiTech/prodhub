package com.swadeshitech.prodhub.services.impl;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.constant.Constants;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.PaginatedResponse;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.integration.cicaptain.config.CiCaptainClient;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildStatusResponse;
import com.swadeshitech.prodhub.integration.kafka.producer.KafkaProducer;
import com.swadeshitech.prodhub.utils.UuidUtil;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.swadeshitech.prodhub.dto.ReleaseCandidateRequest;
import com.swadeshitech.prodhub.dto.ReleaseCandidateResponse;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.ReleaseCandidateStatus;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.ReleaseCandidateService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReleaseCandidateServiceImpl implements ReleaseCandidateService {

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    CiCaptainClient ciCaptainClient;

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

        List<Metadata> buildProfiles = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(request.getBuildProfile())),
                Metadata.class
        );
        if (CollectionUtils.isEmpty(buildProfiles)) {
            log.error("Build profile could not be found {}", request.getBuildProfile());
            throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
        }

        Metadata buildProfile = buildProfiles.getFirst();

        ReleaseCandidate releaseCandidate = new ReleaseCandidate();
        releaseCandidate.setBuildProfile(buildProfile);
        releaseCandidate.setStatus(ReleaseCandidateStatus.CREATED);
        releaseCandidate.setInitiatedBy(user);
        releaseCandidate.setMetaData(request.getMetadata());
        releaseCandidate.setService(applications.getFirst());

        if(StringUtils.hasText(request.getEphemeralEnvironmentName())) {
            List<EphemeralEnvironment> ephemeralEnvironments = readTransactionService.findByDynamicOrFilters(Map.of("_id", request.getEphemeralEnvironmentName())
                    , EphemeralEnvironment.class);
            if (CollectionUtils.isEmpty(ephemeralEnvironments)) {
                log.error("Ephemeral environment could not be found {}", request.getEphemeralEnvironmentName());
                throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_NOT_FOUND);
            }
            releaseCandidate.setEphemeralEnvironmentId(ephemeralEnvironments.getFirst().getId());
        }

        releaseCandidate = writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);
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
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        ReleaseCandidate existingReleaseCandidate = releaseCandidate.getFirst();
        log.info("Updating release candidate with ID: {}", id);
        existingReleaseCandidate.setStatus(ReleaseCandidateStatus.valueOf(request.getReleaseCandidateStatus()));
        existingReleaseCandidate.setMetaData(request.getMetadata());
        writeTransactionService.saveReleaseCandidateToRepository(existingReleaseCandidate);
        return buildResponse(existingReleaseCandidate);
    }

    @Override
    public PaginatedResponse<ReleaseCandidateResponse> getAllReleaseCandidates(String ephemeralEnvironment, Integer page, Integer size, String sortBy, String order) {
        log.info("Fetching release candidates for page {} with size {}", page, size);

        Map<String, Object> filters = createFiltersForReleaseCandidateList(ephemeralEnvironment);

        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<ReleaseCandidate> rcPage = readTransactionService.findByDynamicOrFiltersPaginated(
                filters,
                ReleaseCandidate.class,
                page,
                size,
                sortBy,
                direction
        );
        if (rcPage.isEmpty()) {
            log.warn("No release candidates found");
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        List<ReleaseCandidateResponse> dtoList = rcPage.getContent().stream()
                .map(this::buildResponse)
                .toList();
        return PaginatedResponse.<ReleaseCandidateResponse>builder()
                .content(dtoList)
                .pageNumber(rcPage.getNumber())
                .pageSize(rcPage.getSize())
                .totalElements(rcPage.getTotalElements())
                .totalPages(rcPage.getTotalPages())
                .isLast(rcPage.isLast())
                .build();
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
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        ReleaseCandidate releaseCandidate = releaseCandidates.getFirst();
        String providerID = releaseCandidate.getMetaData().get("providerID");
        Mono<BuildStatusResponse> buildStatusResponseMono = ciCaptainClient.getBuildStatus(providerID,
                releaseCandidate.getMetaData().get("buildRefId"), forceSync);
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

        // Sort release candidates by created time in descending order (newest first)
        releaseCandidates.sort((rc1, rc2) -> rc2.getCreatedTime().compareTo(rc1.getCreatedTime()));

        List<DropdownDTO> dropdownDTOList = new ArrayList<>();

        for (ReleaseCandidate releaseCandidate : releaseCandidates) {
            String timestamp = releaseCandidate.getCreatedTime().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            String key = "Commit: " + releaseCandidate.getMetaData().get("commitId").substring(0, Math.min(7, releaseCandidate.getMetaData().get("commitId").length())) +
                    " | Certified by: " + releaseCandidate.getCertifiedBy().getName() +
                    " | Created: " + timestamp;
                    
            dropdownDTOList.add(DropdownDTO.builder()
                            .key(releaseCandidate.getId())
                            .value(key)
                    .build());
        }

        return dropdownDTOList;
    }

    @Override
    public ReleaseCandidateResponse certifyReleaseCandidateForProduction(String id) {

        // Try to find release candidate with CERTIFIABLE status
        List<ReleaseCandidate> releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(Map.of(
                "_id", new ObjectId(id),
                "status", ReleaseCandidateStatus.CERTIFIABLE));
        
        // If not found, try with CREATED status
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            releaseCandidates = readTransactionService.findReleaseCandidateDetailsByFilters(Map.of(
                    "_id", new ObjectId(id),
                    "status", ReleaseCandidateStatus.CREATED));
        }
        
        if (CollectionUtils.isEmpty(releaseCandidates)) {
            log.error("Release candidates could not be found for id {}", id);
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        ReleaseCandidate releaseCandidate = releaseCandidates.getFirst();
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

    @Override
    public ReleaseCandidate handleReleaseCandidateCreation(PipelineExecution pipelineExecution, String buildStatus) {
        if (!"SUCCESS".equalsIgnoreCase(buildStatus)) {
            log.info("Build status is not SUCCESS for pipeline execution {}, skipping release candidate creation", pipelineExecution.getId());
            return null;
        }

        // Check if release candidate already exists for this pipeline execution
        List<ReleaseCandidate> existingRCs = readTransactionService.findByDynamicOrFilters(
                Map.of("pipelineExecution.$id", new ObjectId(pipelineExecution.getId())),
                ReleaseCandidate.class
        );

        if (!CollectionUtils.isEmpty(existingRCs)) {
            log.info("Release candidate already exists for pipeline execution {}, returning existing one", pipelineExecution.getId());
            return existingRCs.getFirst();
        }

        log.info("Release candidate missing for successful build in pipeline execution {}. Creating manually...", pipelineExecution.getId());

        try {
            // 1. Get Application
            String serviceId = (String) pipelineExecution.getMetaData().get("serviceId");
            if (!StringUtils.hasText(serviceId)) {
                log.error("serviceId not found in pipeline execution metadata {}", pipelineExecution.getId());
                return null;
            }
            List<Application> applications = readTransactionService.findApplicationByFilters(Map.of("_id", new ObjectId(serviceId)));
            if (CollectionUtils.isEmpty(applications)) {
                log.error("Application not found for serviceId {}", serviceId);
                return null;
            }
            Application application = applications.getFirst();

            // 2. Get Build Profile
            String metaDataId = (String) pipelineExecution.getMetaData().get("metaDataId");
            if (!StringUtils.hasText(metaDataId)) {
                log.error("metaDataId not found in pipeline execution metadata {}", pipelineExecution.getId());
                return null;
            }
            List<Metadata> buildProfiles = readTransactionService.findMetaDataByFilters(Map.of("_id", new ObjectId(metaDataId)));
            if (CollectionUtils.isEmpty(buildProfiles)) {
                log.error("Build profile not found for metaDataId {}", metaDataId);
                return null;
            }
            Metadata buildProfile = buildProfiles.getFirst();

            // 3. Extract Metadata from Pipeline Execution
            Map<String, String> metadata = new HashMap<>();
            String buildRefId = (String) pipelineExecution.getMetaData().get("ciCaptainBuildId");
            metadata.put("buildRefId", buildRefId);
            metadata.put("providerID", (String) pipelineExecution.getMetaData().get("providerID"));
            metadata.put("system", "CI-CAPTAIN");

            // Extract parameters from build stage if possible
            if (pipelineExecution.getStageExecutions() != null) {
                for (PipelineExecution.StageExecution stage : pipelineExecution.getStageExecutions()) {
                    if ("build".equalsIgnoreCase(stage.getStageName()) && stage.getTemplate() != null
                            && !CollectionUtils.isEmpty(stage.getTemplate().getSteps())) {
                        for (Template.Step step : stage.getTemplate().getSteps()) {
                            if ("build".equalsIgnoreCase(step.getStepName())) {
                                if (step.getParams() != null) {
                                    metadata.put("repoURL", extractParamValue(step.getParams(), "repoURL"));
                                    metadata.put("branchName", extractParamValue(step.getParams(), "branchName"));
                                    metadata.put("commitId", extractParamValue(step.getParams(), "commitId"));
                                    metadata.put("dockerImageHashValue", extractParamValue(step.getParams(), "dockerImageHashValue"));
                                    metadata.put("buildCommand", extractParamValue(step.getParams(), "buildCommand"));
                                    metadata.put("artifactPath", extractParamValue(step.getParams(), "artifactPath"));
                                }
                                if (step.getMetadata() != null && step.getMetadata().containsKey("buildUrl")) {
                                    metadata.put("buildUrl", String.valueOf(step.getMetadata().get("buildUrl")));
                                }
                                break;
                            }
                        }
                    }
                }
            }

            // 4. Get Ephemeral Environment (optional)
            String ephemeralEnvId = (String) pipelineExecution.getMetaData().get("ephemeralEnvironmentId");
            String ephemeralEnvIdToSet = null;
            if (StringUtils.hasText(ephemeralEnvId)) {
                ephemeralEnvIdToSet = ephemeralEnvId;
            }

            // 5. Create Release Candidate
            User initiatedBy = null;
            if (StringUtils.hasText(pipelineExecution.getCreatedBy())) {
                List<User> users = readTransactionService.findByDynamicOrFilters(
                        Map.of("email", pipelineExecution.getCreatedBy()),
                        User.class
                );
                if (!CollectionUtils.isEmpty(users)) {
                    initiatedBy = users.getFirst();
                }
            }

            ReleaseCandidate releaseCandidate = ReleaseCandidate.builder()
                    .service(application)
                    .buildProfile(buildProfile)
                    .status(ReleaseCandidateStatus.CERTIFIABLE)
                    .metaData(metadata)
                    .buildRefId(buildRefId)
                    .ephemeralEnvironmentId(ephemeralEnvIdToSet)
                    .initiatedBy(initiatedBy)
                    .pipelineExecution(pipelineExecution)
                    .build();

            writeTransactionService.saveReleaseCandidateToRepository(releaseCandidate);
            log.info("Successfully created release candidate manually for pipeline execution: {}", pipelineExecution.getId());
            return releaseCandidate;

        } catch (Exception ex) {
            log.error("Failed to create release candidate manually for pipeline execution: {}", pipelineExecution.getId(), ex);
            return null;
        }
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

        Metadata metadata = releaseCandidate.getBuildProfile();

        ReleaseCandidateResponse response = getReleaseCandidateResponse(releaseCandidate, certifiedBy, initiatedBy);
        if (metadata != null) {
            response.setBuildProfile(metadata.getName().split(Constants.CLONE_METADATA_DELIMITER)[0]);
        }

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

        response.setCreatedBy(releaseCandidate.getCreatedBy());
        response.setCreatedTime(releaseCandidate.getCreatedTime());
        response.setLastModifiedTime(releaseCandidate.getLastModifiedTime());
        response.setLastModifiedBy(releaseCandidate.getLastModifiedBy());
        return response;
    }

    public ReleaseCandidateStatus mapStatusFromCICaptain(String status) {
        return switch (status) {
            case "SUCCESS" -> ReleaseCandidateStatus.CERTIFIABLE;
            case "FAILURE", "FAILED" -> ReleaseCandidateStatus.FAILED;
            case "ABORTED", "CANCELLED" -> ReleaseCandidateStatus.CANCELLED;
            default -> ReleaseCandidateStatus.REJECTED;
        };
    }

    private Map<String, Object> createFiltersForReleaseCandidateList(String ephemeralEnvironment) {
        Map<String, Object> filters = new HashMap<>();
        User user = userService.extractUserFromContext();
        if(StringUtils.hasText(ephemeralEnvironment)) {
            filters.put("ephemeralEnvironment", ephemeralEnvironment);
        } else {
            filters.put("initiatedBy", user);
        }
        return filters;
    }
}
