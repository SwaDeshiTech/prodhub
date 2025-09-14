package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.ApprovalStatus;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.ApprovalService;
import com.swadeshitech.prodhub.services.MetadataService;
import com.swadeshitech.prodhub.services.UserService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private MetadataService metadataService;

    @Override
    public ApprovalResponse createApprovalRequest(ApprovalRequest request) {

        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("_id", new ObjectId(request.getServiceId())));
        if (applications.isEmpty()) {
            log.error("No application found for id: {}", request.getServiceId());
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                Map.of("_id", new ObjectId(request.getProfileId())));
        if (metadataList.isEmpty()) {
            log.error("No metadata found for id: {}", request.getProfileId());
            throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
        }

        Application application = applications.getFirst();
        Metadata profile = metadataList.getFirst();

        ApprovalStage approvalStage = createApprovalStage(application, profile);

        Approvals approvals = new Approvals();
        approvals.setApprovalStatus(ApprovalStatus.PENDING);
        approvals.setApplication(application);
        approvals.setApprovalStage(approvalStage);
        approvals.setCurrentMetadata(profile);
        approvals.setUpdatedMetaData(Base64Util.generateBase64Encoded(request.getMetaData()));
        approvals.setProfileName(profile.getName());
        approvals.setProfileType(profile.getProfileType());

        writeTransactionService.saveApprovalsToRepository(approvals);

        return mapEntityToDTO(approvals);
    }

    @Override
    public ApprovalResponse getApprovalById(String requestId) {

        List<Approvals> approvalList = readTransactionService.findApprovalsByFilters(
                Map.of("_id", new ObjectId(requestId)));
        if (approvalList.isEmpty()) {
            log.error("No approval request found for id: {}", requestId);
            throw new CustomException(ErrorCode.APPROVALS_NOT_FOUND);
        }

        Approvals approvals = approvalList.getFirst();

        return mapEntityToDTO(approvals);
    }

    @Override
    public boolean updateApprovalStatus(String requestId, ApprovalUpdateRequest request) {

        List<Approvals> approvalList = readTransactionService.findApprovalsByFilters(
                Map.of("_id", new ObjectId(requestId)));
        if (approvalList.isEmpty()) {
            log.error("No approval request found for id: {}", requestId);
            throw new CustomException(ErrorCode.APPROVALS_NOT_FOUND);
        }

        Approvals approvals = approvalList.getFirst();
        ApprovalStage stage = approvals.getApprovalStage();
        if (Objects.isNull(stage)) {
            log.error("Approval stage is not attached to approval request: {}", approvals.getId());
            throw new CustomException(ErrorCode.APPROVALS_STAGE_NOT_FOUND);
        }

        validateAndUpdateStatus(stage, request);
        markRequestComplete(approvals);

        return true;
    }

    @Override
    public List<ApprovalResponse> getApprovalsList(ApprovalRequestFilter requestFilter) {

        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("_id", new ObjectId(requestFilter.getServiceId())));
        if (applications.isEmpty()) {
            log.error("No application found for id: {}", requestFilter.getServiceId());
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                Map.of("_id", new ObjectId(requestFilter.getProfileId())));
        if (metadataList.isEmpty()) {
            log.error("No metadata found for id: {}", requestFilter.getProfileId());
            throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
        }

        Application application = applications.getFirst();
        Metadata metadata = metadataList.getFirst();

        List<Approvals> approvalList = readTransactionService.findApprovalsByFilters(
                Map.of("application", application,
                        "profileName", metadata.getName(),
                        "approvalStatus", ApprovalStatus.fromDisplayName(requestFilter.getStatus())
                )
        );

        List<ApprovalResponse> approvalResponseList = new ArrayList<>();

        for(Approvals approvals : approvalList) {
            approvalResponseList.add(ApprovalResponse.builder()
                            .requestId(approvals.getId())
                            .serviceName(approvals.getApplication().getName())
                            .profileType(approvals.getProfileType().getMessage())
                            .createdBy(approvals.getCreatedBy())
                            .description(approvals.getComment())
                    .build());
        }

        return approvalResponseList;
    }

    public void validateAndUpdateStatus(ApprovalStage stage, ApprovalUpdateRequest request) {

        ApprovalStage.Stage lastStage = null, currentStage = null;

        stage.getStages().sort(Comparator.comparingInt(ApprovalStage.Stage::getSequence));

        for (ApprovalStage.Stage itr : stage.getStages()) {
            if (!Objects.isNull(itr) && itr.getName().equals(request.getName())) {
                currentStage = itr;
                break;
            } else {
                lastStage = currentStage;
                currentStage = itr;
            }
        }

        if (Objects.isNull(lastStage)) {
            updateStageStatus(currentStage, request);
        } else {
            if (lastStage.isMandatory() && lastStage.getStatus().equals(ApprovalStatus.APPROVED)) {
                updateStageStatus(currentStage, request);
            } else if (!lastStage.isMandatory()) {
                updateStageStatus(currentStage, request);
            } else {
                log.error("Request cannot be approved {}", stage.getId());
                throw new CustomException(ErrorCode.APPROVALS_STAGE_UPDATE_FAILED);
            }
        }
    }

    private void updateStageStatus(ApprovalStage.Stage stage, ApprovalUpdateRequest request) {
        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (userId != null && stage.getApprovers().contains(userId)) {
            UserResponse userResponse = userService.getUserDetail(userId);
            ApprovalStatus approvalStatus = ApprovalStatus.fromDisplayName(request.getStatus());
            stage.setComments(request.getComments());
            stage.setStatus(approvalStatus);
            stage.setApprovedAt(LocalDate.now());
            stage.setApprovedBy(userResponse.getName() + " (" + userResponse.getEmailId() + ")");
        }
    }

    private ApprovalStage createApprovalStage(Application application, Metadata profile) {

        Team team = application.getTeam();

        List<ApprovalStage.Stage> stages = new ArrayList<>();

        ApprovalStage.Stage qaApproval = new ApprovalStage.Stage();
        qaApproval.setName("QA Approval");
        qaApproval.setApprovers(team.getEmployees().stream().map(User::getId).collect(Collectors.toList()));
        qaApproval.setSequence(1);
        qaApproval.setMandatory(true);
        qaApproval.setStatus(ApprovalStatus.PENDING);

        ApprovalStage.Stage managerApproval = new ApprovalStage.Stage();
        managerApproval.setName("Manager Approval");
        managerApproval.setApprovers(team.getManagers().stream().map(User::getId).collect(Collectors.toList()));
        managerApproval.setSequence(2);
        managerApproval.setMandatory(true);
        managerApproval.setStatus(ApprovalStatus.PENDING);

        return ApprovalStage.builder()
                .description(profile.getProfileType().getMessage())
                .name(profile.getProfileType().getValue())
                .stages(stages)
                .build();

    }

    private ApprovalResponse mapEntityToDTO(Approvals approvals) {
        return ApprovalResponse.builder()
                .requestId(approvals.getId())
                .serviceName(approvals.getApplication().getName())
                .profileName(approvals.getProfileName())
                .profileType(approvals.getProfileType().getMessage())
                .oldMetaData(Base64Util.convertToPlainText(approvals.getCurrentMetadata().getData()))
                .newMetaData(Base64Util.convertToPlainText(approvals.getUpdatedMetaData()))
                .approvalStageResponse(mapApprovalStageEntityToDTO(approvals.getApprovalStage()))
                .createdBy(approvals.getCreatedBy())
                .createdTime(approvals.getCreatedTime())
                .lastModifiedBy(approvals.getLastModifiedBy())
                .lastModifiedTime(approvals.getLastModifiedTime())
                .build();
    }

    private ApprovalResponse.ApprovalStageResponse mapApprovalStageEntityToDTO(ApprovalStage approvalStage) {

        List<ApprovalResponse.ApprovalStageResponse.StageResponse> stageResponseList = new ArrayList<>();

        for(ApprovalStage.Stage stageItr : approvalStage.getStages()) {

            List<String> userDetail = new ArrayList<>();
            for(String userItr : stageItr.getApprovers()) {
                UserResponse userResponse = userService.getUserDetail(userItr);
                if(Objects.isNull(userResponse)) {
                    log.error("User not found {}", userItr);
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                }
                userDetail.add(userResponse.getName() + " (" + userResponse.getEmailId() + ")");
            }

            stageResponseList.add(ApprovalResponse.ApprovalStageResponse.StageResponse.builder()
                            .approvedAt(stageItr.getApprovedAt())
                            .approvedBy(stageItr.getApprovedBy())
                            .status(stageItr.getStatus().getDisplayName())
                            .comments(stageItr.getComments())
                            .sequence(stageItr.getSequence())
                            .isMandatory(stageItr.isMandatory())
                            .name(stageItr.getName())
                            .approvers(userDetail)
                    .build());
        }

        return ApprovalResponse.ApprovalStageResponse.builder()
                .id(approvalStage.getId())
                .name(approvalStage.getName())
                .description(approvalStage.getDescription())
                .stageResponses(stageResponseList)
                .build();
    }

    private void markRequestComplete(Approvals approvals) {
        List<ApprovalStage.Stage> stages = approvals.getApprovalStage().getStages();
        int total = 0, totalCompleted = 0;
        for(ApprovalStage.Stage stage : stages) {
            if(stage.isMandatory()) {
                total++;
                if(ApprovalStatus.APPROVED.equals(stage.getStatus())) {
                    totalCompleted++;
                }
            }
        }
        if(total == totalCompleted) {
            approvals.setApprovalStatus(ApprovalStatus.APPROVED);
            writeTransactionService.saveApprovalsToRepository(approvals);
        }
    }
}
