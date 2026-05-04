package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.PipelineTemplateType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "pipeline_templates")
public class PipelineTemplate extends BaseEntity {

    @Id
    private String id;

    private String name;

    private String version;

    private PipelineTemplateType pipelineTemplateType;

    private boolean isActive;

    private String provider;

    private List<StageDefinition> stages;

    @Data
    @Builder
    public static class StageDefinition {
        private String name;
        private String templateName;
        private int order;
        private boolean stopOnFailure;
        private String version;
    }
}
