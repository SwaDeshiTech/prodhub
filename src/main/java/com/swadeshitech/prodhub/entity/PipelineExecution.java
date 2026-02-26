package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.PipelineStatus;
import com.swadeshitech.prodhub.enums.PipelineStepExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "pipeline_executions")
public class PipelineExecution extends BaseEntity {

    @Id
    private String id;

    private PipelineStatus status;

    private List<StageExecution> stageExecutions;

    private Map<String, Object> metaData;

    @DBRef
    private PipelineTemplate pipelineTemplate;

    @Data
    @Builder
    public static class StageExecution {
        private String id;
        private String stageName;
        private Template template;
        private PipelineStepExecutionStatus status;
        private int order;
        private boolean stopOnFailure;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
