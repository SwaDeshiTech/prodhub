package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.*;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.approval.ApprovalService;
import com.swadeshitech.prodhub.services.DeploymentSetService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

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

        String uuid = UuidUtil.generateFromString(releaseCandidate1.getMetaData().get("dockerImageHashValue"));

        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .serviceId(request.getApplicationId())
                .comment("Deployment Request")
                .metaDataRequest(MetaDataRequest.builder().id(request.getDeploymentProfileId()).build())
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
    public List<DeploymentSetResponse> getDeploymentResponseList() {

        User user = userService.extractUserFromContext();

        List<DeploymentSet> deploymentSets = readTransactionService.findByDynamicOrFilters(Map.of("createdBy", user.getEmailId()), DeploymentSet.class);
        if (CollectionUtils.isEmpty(deploymentSets)) {
            log.error("Deployment sets could not be found");
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_NOT_FOUND);
        }

        List<DeploymentSetResponse> deploymentSetResponses = new ArrayList<>();

        for(DeploymentSet deploymentSet : deploymentSets) {
            deploymentSetResponses.add(DeploymentSetResponse.builder()
                    .id(deploymentSet.getId())
                    .status(deploymentSet.getStatus().getMessage())
                    .deploymentProfileName(deploymentSet.getDeploymentProfile().getName())
                    .serviceName(deploymentSet.getApplication().getName())
                    .createdTime(deploymentSet.getCreatedTime())
                    .metaData(Map.of("COMMIT_ID",
                                    deploymentSet.getReleaseCandidate().getMetaData().get("commitId")))
                    .build());
        }

        deploymentSetResponses.sort(Comparator.comparing(DeploymentSetResponse::getCreatedTime).reversed());

        return deploymentSetResponses;
    }

    @Override
    public void updateDeploymentSet(String id, DeploymentSetUpdateRequest request) {
        List<DeploymentSet> deploymentSets = readTransactionService.findByDynamicOrFilters(
                Map.of("_id", new ObjectId(id)), DeploymentSet.class);
        if (CollectionUtils.isEmpty(deploymentSets)) {
            log.error("Deployment sets could not be found");
            throw new CustomException(ErrorCode.DEPLOYMENT_SET_NOT_FOUND);
        }

        DeploymentSet deploymentSet = deploymentSets.getFirst();
        approvalService.updateApprovalStatus(deploymentSet.getApprovals().getId(), ApprovalUpdateRequest.builder()
                        .comments(request.getComments())
                        .name(request.getApprovalStage())
                        .status(request.getStatus())
                .build());
        if(ApprovalStatus.APPROVED.equals(deploymentSet.getApprovals().getApprovalStatus())) {
            deploymentSet.setStatus(DeploymentSetStatus.COMPLETED);
        } else {
            deploymentSet.setStatus(DeploymentSetStatus.IN_PROGRESS);
        }
        writeTransactionService.saveDeploymentSetToRepository(deploymentSet);
    }

    private DeploymentSetResponse mapDTOToEntity(DeploymentSet deploymentSet) {
        return DeploymentSetResponse.builder()
                .id(deploymentSet.getId())
                .status(deploymentSet.getStatus().getMessage())
                .serviceName(deploymentSet.getApplication().getName())
                .deploymentProfileName(deploymentSet.getDeploymentProfile().getName())
                .buildProfileName(deploymentSet.getReleaseCandidate().getBuildProfile())
                .approvalId(deploymentSet.getApprovals().getId())
                .metaData(Map.of("COMMIT_ID", deploymentSet.getReleaseCandidate().getMetaData().get("commitId")))
                .createdBy(deploymentSet.getCreatedBy())
                .createdTime(deploymentSet.getCreatedTime())
                .lastModifiedBy(deploymentSet.getLastModifiedBy())
                .lastModifiedTime(deploymentSet.getLastModifiedTime())
                .build();
    }
}
