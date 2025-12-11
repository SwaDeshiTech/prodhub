package com.swadeshitech.prodhub.entity;

import com.swadeshitech.prodhub.enums.DeploymentStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deployment_templates")
public class DeploymentTemplate extends BaseEntity {

    @Id
    private String id;

    private String templateName;

    private String version;

    private String description;

    private List<DeploymentStep> steps;

    @Data
    @Builder
    public static class DeploymentStep {

        private int order;

        private String stepName;

        private ChartDetails chartDetails;

        private boolean wait;

        private int timeoutSeconds;

        private Map<String, Object> values;

        private DeploymentStatus status;

        private List<String> params;

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
    }
}