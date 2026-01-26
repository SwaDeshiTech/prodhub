package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.PipelineStatus;
import com.swadeshitech.prodhub.enums.PipelineStepExecutionStatus;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@Document(collection = "pipeline_executions")
public class PipelineExecution extends BaseEntity {

    @Id
    private String id;

    private String pipelineTemplateId;

    private PipelineStatus status;

    private List<StageExecution> stageExecutions;

    @Data
    @Builder
    public static class StageExecution {
        private String stageName;
        private Template template;
        private PipelineStepExecutionStatus status;
        private int order;
        private boolean stopOnFailure;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
