package com.swadeshitech.prodhub.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "pipeline_templates")
public class PipelineTemplate extends BaseEntity {

    @Id
    private String id;

    private String name;

    private String version;

    private boolean isActive;

    private List<StageDefinition> stages;

    @Data
    @Builder
    public static class StageDefinition {
        private String name;
        private String templateName;
        private int order;
        private boolean stopOnFailure;
    }
}
