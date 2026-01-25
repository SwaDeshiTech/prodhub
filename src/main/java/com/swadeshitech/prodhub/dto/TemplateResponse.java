package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.entity.Template;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@SuperBuilder
public class TemplateResponse extends BaseResponse {
    private String id;
    private String templateName;
    private String version;
    private String description;
    List<TemplateStepResponse> stepResponses;

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

    private static List<TemplateResponse.TemplateStepResponse> generateDeploymentTemplateResponse(List<Template.Step> steps) {

        List<TemplateResponse.TemplateStepResponse> deploymentTemplateStepResponseList = new ArrayList<>();

        for(Template.Step step : steps) {
            deploymentTemplateStepResponseList.add(TemplateStepResponse.builder()
                    .status(step.getStatus().getMessage())
                    .stepName(step.getStepName())
                    .wait(step.isWait())
                    .timeoutSeconds(step.getTimeoutSeconds())
                    .params(step.getParams())
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
