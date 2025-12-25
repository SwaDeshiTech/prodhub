package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@SuperBuilder
public class ApprovalResponse extends BaseResponse {
    private String requestId;
    private String serviceName;
    private String description;
    private String profileType;
    private String profileName;
    private String oldMetaData;
    private String newMetaData;
    private String status;
    private ApprovalStageResponse approvalStageResponse;

    @Data
    @Builder
    public static class ApprovalStageResponse {
        private String id;
        private String name;
        private String description;
        private List<StageResponse> stageResponses;

        @Data
        @Builder
        public static class StageResponse {
            private String name;
            private String comments;
            private String status;
            private List<String> approvers;
            private boolean isMandatory;
            private int sequence;
            private String approvedBy;
            private LocalDate approvedAt;
        }
    }
}
