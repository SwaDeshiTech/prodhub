package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.StepExecutionStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "templates")
public class Template extends BaseEntity {

    @Id
    private String id;

    private String templateName;

    private String version;

    private String description;

    private Set<Step> steps;

    @Data
    @Builder
    public static class Step {

        private int order;

        private String stepName;

        private ChartDetails chartDetails;

        private boolean wait;

        private int timeoutSeconds;

        private Map<String, Object> values;

        private StepExecutionStatus status;

        private Map<String, TemplateStepParam> params;

        private Map<String, Object> metadata;

        private boolean skipStep;

        @Data
        @Builder
        public static class ChartDetails {

            private String repository;

            private String chartName;

            private String version;

            private String chartLink;
        }

        @Data
        @Builder
        public static class TemplateStepParam {

            private String affectedKey;

            private String displayName;

            private String value;
        }
    }
}