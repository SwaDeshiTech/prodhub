package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.*;
import com.swadeshitech.prodhub.exception.CustomException;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
                    .deploymentProfileName(deploymentSet.getDeploymentProfile().getName().split("::")[0])
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
        List<DeploymentResponse> deploymentResponse = new ArrayList<>();
        if(Objects.nonNull(deploymentSet.getDeployments()) && !deploymentSet.getDeployments().isEmpty()) {
            for(Deployment deployment : deploymentSet.getDeployments()) {
                deploymentResponse.add(DeploymentResponse.builder()
                                .deploymentID(deployment.getId())
                                .status(deployment.getStatus().getMessage())
                                .createdBy(deployment.getCreatedBy())
                                .createdTime(deployment.getCreatedTime())
                        .build());
            }
        }
        return DeploymentSetResponse.builder()
                .id(deploymentSet.getId())
                .status(deploymentSet.getStatus().getMessage())
                .serviceName(deploymentSet.getApplication().getName())
                .deploymentProfileName(deploymentSet.getDeploymentProfile().getName().split("::")[0])
                .buildProfileName(deploymentSet.getReleaseCandidate().getBuildProfile())
                .approvalId(deploymentSet.getApprovals().getId())
                .metaData(Map.of("COMMIT_ID", deploymentSet.getReleaseCandidate().getMetaData().get("commitId")))
                .deployments(deploymentResponse)
                .createdBy(deploymentSet.getCreatedBy())
                .createdTime(deploymentSet.getCreatedTime())
                .lastModifiedBy(deploymentSet.getLastModifiedBy())
                .lastModifiedTime(deploymentSet.getLastModifiedTime())
                .build();
    }
}
