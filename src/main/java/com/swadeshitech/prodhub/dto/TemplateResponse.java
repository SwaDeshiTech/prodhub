package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.entity.Template;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class TemplateResponse extends BaseResponse {
    private String id;
    private String templateName;
    private String version;
    private String description;
    private Set<TemplateStepResponse> stepResponses;

    @Data
    @Builder
    public static class TemplateStepResponse {
        private int order;
        private String stepName;
        private ChartDetailsResponse chartDetails;
        private boolean wait;
        private int timeoutSeconds;
        private Map<String, Object> values;
        private Map<String, Template.Step.TemplateStepParam> params;
        private String status;
        private Map<String, Object> metaData;

        @Data
        @Builder
        public static class ChartDetailsResponse {
            private String repository;
            private String chartName;
            private String version;
            private String chartLink;
        }
    }

    public static TemplateResponse mapDTOToEntity(Template template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .description(template.getDescription())
                .version(template.getVersion())
                .stepResponses(generateDeploymentTemplateResponse(template.getSteps()))
                .createdTime(template.getCreatedTime())
                .createdBy(template.getCreatedBy())
                .lastModifiedTime(template.getLastModifiedTime())
                .lastModifiedBy(template.getLastModifiedBy())
                .build();
    }

    private static Set<TemplateResponse.TemplateStepResponse> generateDeploymentTemplateResponse(Set<Template.Step> steps) {

        Set<TemplateResponse.TemplateStepResponse> deploymentTemplateStepResponseList = new HashSet<>();

        for(Template.Step step : steps) {
            // Clean up step metadata by removing ID fields and keeping only name fields
            Map<String, Object> cleanedMetaData = null;
            if (step.getMetadata() != null && !step.getMetadata().isEmpty()) {
                cleanedMetaData = new HashMap<>(step.getMetadata());
                // Remove ID fields
                cleanedMetaData.remove("metaDataId");
                cleanedMetaData.remove("buildProfileId");
                // If buildProfile name is not present but ID was, we can't replace it here
                // since we don't have access to the metadata service in this static method
                // The buildProfile name should already be in the metadata for new executions
            }
            
            deploymentTemplateStepResponseList.add(TemplateStepResponse.builder()
                    .status(step.getStatus().getMessage())
                    .stepName(step.getStepName())
                    .wait(step.isWait())
                    .timeoutSeconds(step.getTimeoutSeconds())
                    .params(step.getParams())
                    .order(step.getOrder())
                    .chartDetails(generateChartDetailsResponse(step.getChartDetails()))
                    .metaData(cleanedMetaData)
                    .build());
        }
        return deploymentTemplateStepResponseList;
    }

    private static TemplateResponse.TemplateStepResponse.ChartDetailsResponse generateChartDetailsResponse(Template.Step.ChartDetails chartDetails) {
        if (Objects.isNull(chartDetails)) {
            return null;
        }
        return TemplateResponse.TemplateStepResponse.ChartDetailsResponse.builder()
                .chartName(chartDetails.getChartName())
                .repository(chartDetails.getRepository())
                .version(chartDetails.getVersion())
                .chartLink(chartDetails.getChartLink()).build();
    }
}
