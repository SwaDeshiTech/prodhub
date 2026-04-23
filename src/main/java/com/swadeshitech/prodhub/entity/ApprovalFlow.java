package com.swadeshitech.prodhub.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "approval_flows")
@EqualsAndHashCode(callSuper = true)
@Builder
public class ApprovalFlow extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String key; // Unique identifier for the flow (e.g., "common", "critical-deployment")

    private String name; // Display name

    private String description;

    private boolean isDefault; // If true, this is the common/fallback flow

    private boolean isActive;

    private List<FlowStage> stages; // Approval stages in sequence

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlowStage {
        private String name; // Stage name (e.g., "Manager Approval", "Tech Lead Approval")
        private String description;
        private int sequence; // Order of execution
        private boolean isMandatory; // If true, this stage must be completed
        private List<String> approverRoleIds; // Role IDs that can approve this stage
        private List<String> approverUserIds; // Specific user IDs that can approve this stage
        private int minApprovalsRequired; // Minimum number of approvals required for this stage
        private boolean requireAllApprovers; // If true, all approvers must approve
    }
}
