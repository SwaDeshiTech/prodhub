package com.swadeshitech.prodhub.services.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.enums.ApprovalStatus;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.ProfileType;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.service.ServiceApprovalConfigService;
import com.swadeshitech.prodhub.services.OnboardingService;
import com.swadeshitech.prodhub.services.UserService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Qualifier("ApprovalServiceImpl")
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OnboardingService onboardingService;

    @Autowired
    @Qualifier("DeploymentApprovalImpl")
    ApprovalService deploymentApprovalService;

    @Autowired
    private ServiceApprovalConfigService serviceApprovalConfigService;

    @Override
    public ApprovalResponse createApprovalRequest(ApprovalRequest request) {

        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("_id", new ObjectId(request.getServiceId())));
        if (applications.isEmpty()) {
            log.error("No application found for id: {}", request.getServiceId());
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                Map.of("_id", new ObjectId(request.getMetaDataRequest().getId())));
        if (metadataList.isEmpty()) {
            log.error("No metadata found for id: {}", request.getMetaDataRequest().getId());
            throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
        }

        Application application = applications.getFirst();
        Metadata profile = metadataList.getFirst();

        ApprovalStage approvalStage = createApprovalStage(application, profile);
        String metadataRequest;

        try {
            metadataRequest = objectMapper.writeValueAsString(request.getMetaDataRequest());
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.METADATA_PROFILE_INVALID_DATA);
        }

        Approvals approvals = new Approvals();
        approvals.setApprovalStatus(ApprovalStatus.PENDING);
        approvals.setApplication(application);
        approvals.setApprovalStage(approvalStage);
        approvals.setCurrentMetadata(profile);
        approvals.setUpdatedMetaData(Base64Util.generateBase64Encoded(metadataRequest));
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

        if (ProfileType.DEPLOYMENT.equals(approvals.getProfileType())) {
            deploymentApprovalService.updateApprovalStatus(requestId, request);
        } else {
            ApprovalStage stage = approvals.getApprovalStage();
            if (Objects.isNull(stage)) {
                log.error("Approval stage is not attached to approval request: {}", approvals.getId());
                throw new CustomException(ErrorCode.APPROVALS_STAGE_NOT_FOUND);
            }
            validateAndUpdateStatus(stage, request);
            markRequestComplete(approvals);
        }
        return true;
    }

    @Override
    public PaginatedResponse<ApprovalResponse> getApprovalsList(ApprovalRequestFilter requestFilter, Integer page, Integer size, String sortBy, String order) {

        Map<String, Object> filters = generateApprovalFilters(requestFilter);
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<ApprovalStage> pageApprovalStageList = readTransactionService.findByDynamicOrFiltersPaginated(
                filters,
                ApprovalStage.class,
                page,
                size,
                sortBy,
                direction
        );

        List<ApprovalResponse> approvalResponseList = new ArrayList<>();
        for (ApprovalStage stage : pageApprovalStageList.getContent()) {
            Approvals approvals = stage.getApprovals();
            approvalResponseList.add(ApprovalResponse.builder()
                    .requestId(approvals.getId())
                    .serviceName(approvals.getApplication().getName())
                    .profileType(approvals.getProfileType().getMessage())
                    .description(approvals.getComment())
                    .status(approvals.getApprovalStatus().getDisplayName())
                    .createdBy(approvals.getCreatedBy())
                    .createdTime(approvals.getCreatedTime())
                    .build());
        }

        return PaginatedResponse.<ApprovalResponse>builder()
                .content(approvalResponseList)
                .pageNumber(pageApprovalStageList.getNumber())
                .pageSize(pageApprovalStageList.getSize())
                .totalElements(pageApprovalStageList.getTotalElements())
                .totalPages(pageApprovalStageList.getTotalPages())
                .isLast(pageApprovalStageList.isLast())
                .build();
    }

    private Map<String, Object> generateApprovalFilters(ApprovalRequestFilter requestFilter) {

        Map<String, Object> filters = new HashMap<>();

        if (StringUtils.hasText(requestFilter.getServiceId())) {
            List<Application> applications = readTransactionService.findApplicationByFilters(
                    Map.of("_id", new ObjectId(requestFilter.getServiceId())));
            if (applications.isEmpty()) {
                log.error("No application found for id: {}", requestFilter.getServiceId());
                throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
            }
            filters.put("application", applications.getFirst());
        }

        if (StringUtils.hasText(requestFilter.getProfileId())) {
            List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(
                    Map.of("_id", new ObjectId()));
            if (metadataList.isEmpty()) {
                log.error("No metadata found for id: {}", requestFilter.getProfileId());
                throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
            }
            filters.put("profileName", metadataList.getFirst().getName());
        }

        if (StringUtils.hasText(requestFilter.getStatus())) {
            filters.put("approvalStatus", ApprovalStatus.fromDisplayName(requestFilter.getStatus()));
        }

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (StringUtils.hasText(userId)) {
            filters.put("stages.approvers", List.of(userId));
        }
        return filters;
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
        writeTransactionService.saveApprovalStageToRepository(stage);
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
        // Try to get custom approval flow for the service, with fallback to default
        ApprovalFlow approvalFlow = serviceApprovalConfigService.getApprovalFlowForServiceWithFallback(application.getId());

        List<ApprovalStage.Stage> stages = new ArrayList<>();

        if (approvalFlow != null && approvalFlow.getStages() != null && !approvalFlow.getStages().isEmpty()) {
            // Use the configured approval flow
            for (ApprovalFlow.FlowStage flowStage : approvalFlow.getStages()) {
                ApprovalStage.Stage stage = new ApprovalStage.Stage();
                stage.setName(flowStage.getName());
                stage.setComments(flowStage.getDescription());
                stage.setSequence(flowStage.getSequence());
                stage.setMandatory(flowStage.isMandatory());
                stage.setStatus(ApprovalStatus.PENDING);

                // Resolve approvers from roles or specific users
                List<String> approvers = new ArrayList<>();
                if (flowStage.getApproverUserIds() != null) {
                    approvers.addAll(flowStage.getApproverUserIds());
                }
                if (flowStage.getApproverRoleIds() != null) {
                    // Resolve users from roles
                    approvers.addAll(resolveUsersFromRoles(flowStage.getApproverRoleIds(), application));
                }
                stage.setApprovers(approvers);

                stages.add(stage);
            }
        } else {
            // Fallback to default hardcoded stages
            Team team = application.getTeam();
            if (team != null) {
                if (team.getEmployees() != null) {
                    ApprovalStage.Stage qaApproval = new ApprovalStage.Stage();
                    qaApproval.setName("QA Approval");
                    qaApproval.setApprovers(team.getEmployees().stream().map(User::getId).collect(Collectors.toList()));
                    qaApproval.setSequence(1);
                    qaApproval.setMandatory(true);
                    qaApproval.setStatus(ApprovalStatus.PENDING);
                    stages.add(qaApproval);
                }

                if (team.getManagers() != null) {
                    ApprovalStage.Stage managerApproval = new ApprovalStage.Stage();
                    managerApproval.setName("Manager Approval");
                    managerApproval.setApprovers(team.getManagers().stream().map(User::getId).collect(Collectors.toList()));
                    managerApproval.setSequence(2);
                    managerApproval.setMandatory(true);
                    managerApproval.setStatus(ApprovalStatus.PENDING);
                    stages.add(managerApproval);
                }
            }
        }

        return ApprovalStage.builder()
                .description(profile.getProfileType().getMessage())
                .name(profile.getProfileType().getValue())
                .stages(stages)
                .build();
    }

    private List<String> resolveUsersFromRoles(List<String> roleIds, Application application) {
        List<String> userIds = new ArrayList<>();
        // Resolve users based on roles
        // This is a placeholder - implement actual role resolution logic
        // You might need to query the Role entity and find users with those roles
        return userIds;
    }

    private ApprovalResponse mapEntityToDTO(Approvals approvals) {
        String oldMetaData = "", newMetaData = "";

        if (Objects.nonNull(approvals.getCurrentMetadata())) {
            oldMetaData = Base64Util.convertToPlainText(approvals.getCurrentMetadata().getData());
        }
        if (Objects.nonNull(approvals.getUpdatedMetaData())) {
            newMetaData = Base64Util.convertToPlainText(approvals.getUpdatedMetaData());
        }

        return ApprovalResponse.builder()
                .requestId(approvals.getId())
                .serviceName(approvals.getApplication().getName())
                .profileName(approvals.getProfileName())
                .profileType(approvals.getProfileType().getMessage())
                .oldMetaData(oldMetaData)
                .newMetaData(newMetaData)
                .approvalStageResponse(mapApprovalStageEntityToDTO(approvals.getApprovalStage()))
                .createdBy(approvals.getCreatedBy())
                .createdTime(approvals.getCreatedTime())
                .lastModifiedBy(approvals.getLastModifiedBy())
                .lastModifiedTime(approvals.getLastModifiedTime())
                .build();
    }

    private ApprovalResponse.ApprovalStageResponse mapApprovalStageEntityToDTO(ApprovalStage approvalStage) {

        List<ApprovalResponse.ApprovalStageResponse.StageResponse> stageResponseList = new ArrayList<>();

        for (ApprovalStage.Stage stageItr : approvalStage.getStages()) {

            List<String> userDetail = new ArrayList<>();
            for (String userItr : stageItr.getApprovers()) {
                UserResponse userResponse = userService.getUserDetail(userItr);
                if (Objects.isNull(userResponse)) {
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
        for (ApprovalStage.Stage stage : stages) {
            if (stage.isMandatory()) {
                total++;
                if (ApprovalStatus.APPROVED.equals(stage.getStatus())) {
                    totalCompleted++;
                } else {
                    return;
                }
            }
        }
        if (total == totalCompleted) {
            approvals.setApprovalStatus(ApprovalStatus.APPROVED);
            writeTransactionService.saveApprovalsToRepository(approvals);
            String data = Base64Util.convertToPlainText(approvals.getUpdatedMetaData());
            MetaDataRequest metaDataRequest = objectMapper.convertValue(data, MetaDataRequest.class);
            ApplicationProfileRequest applicationRequest = ApplicationProfileRequest.builder()
                    .applicationId(approvals.getApplication().getId())
                    .profile(metaDataRequest)
                    .initiatedBy(approvals.getCreatedBy())
                    .build();
            onboardingService.onboardProfile(applicationRequest);
        }
    }
}
