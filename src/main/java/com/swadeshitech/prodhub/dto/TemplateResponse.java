package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.entity.Template;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder
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
        private List<String> params;
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
            deploymentTemplateStepResponseList.add(TemplateStepResponse.builder()
                    .status(step.getStatus().getMessage())
                    .stepName(step.getStepName())
                    .wait(step.isWait())
                    .timeoutSeconds(step.getTimeoutSeconds())
                    //.params(step.getParams())
                    .order(step.getOrder())
                    .chartDetails(generateChartDetailsResponse(step.getChartDetails()))
                    .metaData(step.getMetadata())
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
