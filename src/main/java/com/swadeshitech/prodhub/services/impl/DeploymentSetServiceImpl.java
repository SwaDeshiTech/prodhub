package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.*;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.integration.storage.FileUploadResponse;
import com.swadeshitech.prodhub.services.FileUploadService;
import com.swadeshitech.prodhub.services.MetadataService;
import com.swadeshitech.prodhub.services.approval.ApprovalService;
import com.swadeshitech.prodhub.services.DeploymentSetService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.swadeshitech.prodhub.constant.Constants.DEPLOYMENT_SET_ID_KEY;

@Service
@Slf4j
public class DeploymentSetServiceImpl implements DeploymentSetService {

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    @Qualifier("DeploymentApprovalImpl")
    ApprovalService approvalService;

    @Autowired
    MetadataService metadataService;

    @Autowired
    FileUploadService fileUploadService;

    @Override
    public String createDeploymentSet(DeploymentSetRequest request) {

        List<Application> applications = readTransactionService
                .findApplicationByFilters(Map.of("_id", new ObjectId(request.getApplicationId())));
        if (CollectionUtils.isEmpty(applications)) {
            log.error("Failed to create deployment set, service not found {}", request.getApplicationId());
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", new ObjectId(request.getReleaseCandidateId()));
        filters.put("service", applications.getFirst());
        filters.put("status", ReleaseCandidateStatus.CERTIFIED);

        List<ReleaseCandidate> releaseCandidate = readTransactionService.findReleaseCandidateDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(releaseCandidate)) {
            log.error("Release candidate with ID: {} not found", request.getReleaseCandidateId());
            throw new CustomException(ErrorCode.RELEASE_CANDIDATE_NOT_FOUND);
        }

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                Map.of("_id", new ObjectId(request.getDeploymentProfileId()),
                        "profileType", ProfileType.DEPLOYMENT
                ));
        if (CollectionUtils.isEmpty(metadataList)) {
            log.error("Deployment profile with ID: {} not found", request.getDeploymentProfileId());
            throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
        }

        ReleaseCandidate releaseCandidate1 = releaseCandidate.getFirst();

        String clonedProfileName = metadataList.getFirst().getName() + "::" + "DeploymentProfile" + "::" + releaseCandidate1.getMetaData().get("commitId");

        Metadata clonedMetaDataProfile = metadataService.cloneProfile(metadataList.getFirst().getId(), clonedProfileName);

        String uuid = UuidUtil.generateFromString(releaseCandidate1.getMetaData().get("dockerImageHashValue"));

        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .serviceId(request.getApplicationId())
                .comment("Deployment Request")
                .metaDataRequest(MetaDataRequest.builder().id(request.getDeploymentProfileId()).build())
                .metaData(Map.of(DEPLOYMENT_SET_ID_KEY, uuid))
                .build();

        ApprovalResponse approvalResponse = approvalService.createApprovalRequest(approvalRequest);

        List<Approvals> approvals = readTransactionService.findApprovalsByFilters(Map.of("_id", new ObjectId(approvalResponse.getRequestId())));
        if (CollectionUtils.isEmpty(approvals)) {
            log.error("Approval set could not be found {}", approvalResponse.getId());
            throw new CustomException(ErrorCode.APPROVALS_NOT_FOUND);
        }
        Approvals approval = approvals.getFirst();

        DeploymentSet deploymentSet = DeploymentSet.builder()
                .uuid(uuid)
                .status(DeploymentSetStatus.CREATED)
                .application(applications.getFirst())
                .releaseCandidate(releaseCandidate.getFirst())
                .deploymentProfile(metadataList.getFirst())
                .approvals(approval)
                .deploymentProfile(clonedMetaDataProfile)
                .build();

        deploymentSet = writeTransactionService.saveDeploymentSetToRepository(deploymentSet);

        return deploymentSet.getId();
    }

    @Override
    public DeploymentSetResponse getDeploymentSetDetails(String id) {

        List<DeploymentSet> deploymentSets = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(id)), DeploymentSet.class);
        if(CollectionUtils.isEmpty(deploymentSets)) {
            log.error("Deployment set could not be found {}", id);
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_NOT_FOUND);
        }

        return mapDTOToEntity(deploymentSets.getFirst());
    }

    @Override
    public PaginatedResponse<DeploymentSetResponse> getDeploymentResponseList(Integer page, Integer size, String sortBy, String order) {

        log.info("Fetching deployment sets for page {} with size {}", page, size);
        User user = userService.extractUserFromContext();

        Map<String, Object> filters = new HashMap<>();
        filters.put("createdBy", user.getEmailId());
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Page<DeploymentSet> deploymentSetsPage = readTransactionService.findByDynamicOrFiltersPaginated(
                filters,
                DeploymentSet.class,
                page,
                size,
                sortBy,
                direction
        );

        if (deploymentSetsPage.isEmpty()) {
            log.warn("No deployment sets found");
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_NOT_FOUND);
        }

        List<DeploymentSetResponse> deploymentSetResponses = new ArrayList<>();

        for(DeploymentSet deploymentSet : deploymentSetsPage.getContent()) {
            deploymentSetResponses.add(DeploymentSetResponse.builder()
                    .id(deploymentSet.getId())
                    .status(deploymentSet.getStatus().getMessage())
                    .deploymentProfileName(deploymentSet.getDeploymentProfile().getName().split("::")[0])
                    .serviceName(deploymentSet.getApplication().getName())
                    .createdTime(deploymentSet.getCreatedTime())
                    .metaData(Map.of("COMMIT_ID",
                                    deploymentSet.getReleaseCandidate().getMetaData().get("commitId")))
                    .build());
        }

        return PaginatedResponse.<DeploymentSetResponse>builder()
                .content(deploymentSetResponses)
                .pageNumber(deploymentSetsPage.getNumber())
                .pageSize(deploymentSetsPage.getSize())
                .totalElements(deploymentSetsPage.getTotalElements())
                .totalPages(deploymentSetsPage.getTotalPages())
                .isLast(deploymentSetsPage.isLast())
                .build();
    }

    @Override
    public void updateDeploymentSet(String id, DeploymentSetUpdateRequest request) {

        DeploymentSet deploymentSet = fetchDeploymentSet(Map.of("_id", new ObjectId(id)));
        approvalService.updateApprovalStatus(deploymentSet.getApprovals().getId(), ApprovalUpdateRequest.builder()
                        .comments(request.getComments())
                        .name(request.getName())
                        .status(request.getStatus())
                .build());
        deploymentSet = fetchDeploymentSet(Map.of("_id", new ObjectId(id)));
        updateDeploymentSetStatusByApprovalStatus(deploymentSet);
        writeTransactionService.saveDeploymentSetToRepository(deploymentSet);
    }

    @Override
    public void updateDeploymentSetStatus(String deploymentSetIDUUID) {
        DeploymentSet deploymentSet = fetchDeploymentSet(Map.of("uuid", deploymentSetIDUUID));
        updateDeploymentSetStatusByApprovalStatus(deploymentSet);
        writeTransactionService.saveDeploymentSetToRepository(deploymentSet);
    }

    private void updateDeploymentSetStatusByApprovalStatus(DeploymentSet deploymentSet) {
        ApprovalStatus status = deploymentSet.getApprovals().getApprovalStatus();
        log.info("Printing deployment set ID {} and status {}", deploymentSet.getId(), status);
        if(ApprovalStatus.APPROVED.equals(status)) {
            deploymentSet.setStatus(DeploymentSetStatus.COMPLETED);
        } else if(ApprovalStatus.PENDING.equals(status)) {
            deploymentSet.setStatus(DeploymentSetStatus.IN_PROGRESS);
        } else if(ApprovalStatus.CANCELED.equals(status) || ApprovalStatus.REJECTED.equals(status)) {
            deploymentSet.setStatus(DeploymentSetStatus.FAILED);
        }
    }

    private DeploymentSet fetchDeploymentSet(Map<String, Object> filters) {
        List<DeploymentSet> deploymentSets = readTransactionService.findByDynamicOrFilters(
                filters, DeploymentSet.class);
        if (CollectionUtils.isEmpty(deploymentSets)) {
            log.error("Deployment sets could not be found");
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_NOT_FOUND);
        }

        return deploymentSets.getFirst();
    }

    private DeploymentSetResponse mapDTOToEntity(DeploymentSet deploymentSet) {
        List<PipelineExecutionDetailsDTO> pipelineExecutionResponses = new ArrayList<>();
        if(Objects.nonNull(deploymentSet.getPipelineExecutions()) && !deploymentSet.getPipelineExecutions().isEmpty()) {
            for(PipelineExecution pipelineExecution : deploymentSet.getPipelineExecutions()) {
                pipelineExecutionResponses.add(PipelineExecutionDetailsDTO.builder()
                                .id(pipelineExecution.getId())
                                .status(pipelineExecution.getStatus())
                                .createdBy(pipelineExecution.getCreatedBy())
                                .createdTime(pipelineExecution.getCreatedTime())
                                .metaData(pipelineExecution.getMetaData())
                        .build());
            }
        }
        return DeploymentSetResponse.builder()
                .id(deploymentSet.getId())
                .status(deploymentSet.getStatus().getMessage())
                .serviceName(deploymentSet.getApplication().getName())
                .deploymentProfileName(deploymentSet.getDeploymentProfile().getName().split("::")[0])
                .buildProfileName(deploymentSet.getReleaseCandidate().getBuildProfile().getName())
                .approvalId(deploymentSet.getApprovals().getId())
                .metaData(Map.of("COMMIT_ID", deploymentSet.getReleaseCandidate().getMetaData().get("commitId")))
                .pipelineExecutions(pipelineExecutionResponses)
                .createdBy(deploymentSet.getCreatedBy())
                .createdTime(deploymentSet.getCreatedTime())
                .lastModifiedBy(deploymentSet.getLastModifiedBy())
                .lastModifiedTime(deploymentSet.getLastModifiedTime())
                .build();
    }

    @Override
    public FileUploadResponse uploadEvidenceFile(String deploymentSetId, MultipartFile file) {
        try {
            // Get deployment set details
            DeploymentSet deploymentSet = fetchDeploymentSet(Map.of("_id", new ObjectId(deploymentSetId)));
            
            // Extract service name and commit ID
            String serviceName = deploymentSet.getApplication().getName();
            String commitId = deploymentSet.getReleaseCandidate().getMetaData().get("commitId");
            
            // Get metadataId from deployment set's metadata
            Object metadataIdObj = deploymentSet.getMetaData() != null 
                    ? deploymentSet.getMetaData().get("evidenceRepositoryCredentialProviderId") 
                    : null;
            
            if (metadataIdObj == null) {
                return FileUploadResponse.builder()
                        .success(false)
                        .message("Evidence repository credential provider ID not found in deployment set metadata")
                        .build();
            }
            
            String metadataId = metadataIdObj.toString();
            
            // Always construct file name as deploymentSetId_commitId.extension
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String finalFileName = deploymentSetId + "_" + commitId + "." + extension;
            
            // Build folder path: evidence/{serviceName}/...
            String folderPath = deploymentSetId;
            
            // Call file upload service
            return fileUploadService.uploadFile(
                    metadataId, 
                    file, 
                    serviceName, 
                    finalFileName, 
                    folderPath, 
                    null
            );
            
        } catch (Exception e) {
            log.error("Error uploading evidence file for deployment set: {}", deploymentSetId, e);
            return FileUploadResponse.builder()
                    .success(false)
                    .message("Error uploading evidence file: " + e.getMessage())
                    .build();
        }
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    @Override
    public String triggerPipelineExecution(String deploymentSetId) {
        // Fetch deployment set
        DeploymentSet deploymentSet = fetchDeploymentSet(Map.of("_id", new ObjectId(deploymentSetId)));
        
        // Extract pipelineTemplateId from deployment profile metadata
        String pipelineTemplateId = null;
        try {
            if (deploymentSet.getDeploymentProfile() != null && deploymentSet.getDeploymentProfile().getData() != null) {
                String profileData = deploymentSet.getDeploymentProfile().getData();
                if (StringUtils.hasText(profileData)) {
                    Map<String, Object> data = objectMapper.readValue(
                            com.swadeshitech.prodhub.utils.Base64Util.convertToPlainText(profileData), Map.class);
                    pipelineTemplateId = (String) data.get("pipelineTemplateId");
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract pipelineTemplateId from deployment profile metadata for deployment set: {}", deploymentSetId, e);
        }
        
        if (!StringUtils.hasText(pipelineTemplateId)) {
            log.error("pipelineTemplateId not found in deployment profile metadata for deployment set: {}", deploymentSetId);
            throw new CustomException(ErrorCode.PIPELINE_TEMPLATE_COULD_NOT_BE_FOUND);
        }
        
        // Build pipeline execution request with deployment set metadata
        Map<String, String> metaData = new HashMap<>();
        metaData.put("deploymentSetId", deploymentSetId);
        metaData.put("releaseCandidateId", deploymentSet.getReleaseCandidate().getId());
        metaData.put("imageTag", deploymentSet.getReleaseCandidate().getMetaData().get("dockerImageHashValue"));

        PipelineExecutionRequest pipelineExecutionRequest = PipelineExecutionRequest.builder()
                .metaDataID(deploymentSet.getDeploymentProfile().getId())
                .metaData(metaData)
                .build();

        String pipelineExecutionId = pipelineService.schedulePipelineExecution(pipelineExecutionRequest);
        log.info("Pipeline execution started with ID: {} for deployment set: {}", pipelineExecutionId, deploymentSetId);

        // Associate the pipeline execution with the deployment set
        List<PipelineExecution> pipelineExecutions = deploymentSet.getPipelineExecutions();
        if (CollectionUtils.isEmpty(pipelineExecutions)) {
            pipelineExecutions = new ArrayList<>();
        }
        PipelineExecution pipelineExecution = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(pipelineExecutionId)), PipelineExecution.class).getFirst();
        pipelineExecutions.add(pipelineExecution);
        deploymentSet.setPipelineExecutions(pipelineExecutions);
        writeTransactionService.saveDeploymentSetToRepository(deploymentSet);

        return pipelineExecutionId;
    }

    @Autowired
    private com.swadeshitech.prodhub.services.PipelineService pipelineService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
}
