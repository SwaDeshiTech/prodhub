package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.ApprovalStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "approvals_stage")
public class ApprovalStage extends BaseEntity {

    @Id
    private String id;

    private String name;

    private String description;

    private List<Stage> stages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Stage {

        private String name;

        private String comments;

        private ApprovalStatus status;

        private List<String> approvers;

        private boolean isMandatory;

        private int sequence;

        private String approvedBy;

        private LocalDate approvedAt;
    }
}
