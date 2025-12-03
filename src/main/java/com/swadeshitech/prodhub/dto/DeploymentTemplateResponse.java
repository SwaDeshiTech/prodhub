package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.entity.DeploymentTemplate;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
public class DeploymentTemplateResponse extends BaseResponse {
    private String id;
    private String templateName;
    private String version;
    private String description;
    List<DeploymentTemplateStepResponse> stepResponses;

    @Data
    @Builder
    public static class DeploymentTemplateStepResponse {
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

    public static DeploymentTemplateResponse mapDTOToEntity(DeploymentTemplate deploymentTemplate) {
        return DeploymentTemplateResponse.builder()
                .id(deploymentTemplate.getId())
                .templateName(deploymentTemplate.getTemplateName())
                .description(deploymentTemplate.getDescription())
                .version(deploymentTemplate.getVersion())
                .stepResponses(generateDeploymentTemplateResponse(deploymentTemplate.getSteps()))
                .createdTime(deploymentTemplate.getCreatedTime())
                .createdBy(deploymentTemplate.getCreatedBy())
                .lastModifiedTime(deploymentTemplate.getLastModifiedTime())
                .lastModifiedBy(deploymentTemplate.getLastModifiedBy())
                .build();
    }

    private static List<DeploymentTemplateResponse.DeploymentTemplateStepResponse> generateDeploymentTemplateResponse(List<DeploymentTemplate.DeploymentStep> steps) {

        List<DeploymentTemplateResponse.DeploymentTemplateStepResponse> deploymentTemplateStepResponseList = new ArrayList<>();

        for(DeploymentTemplate.DeploymentStep step : steps) {
            deploymentTemplateStepResponseList.add(DeploymentTemplateStepResponse.builder()
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

    private static DeploymentTemplateResponse.DeploymentTemplateStepResponse.ChartDetailsResponse generateChartDetailsResponse(DeploymentTemplate.DeploymentStep.ChartDetails chartDetails) {
        return DeploymentTemplateResponse.DeploymentTemplateStepResponse.ChartDetailsResponse.builder()
                .chartName(chartDetails.getChartName())
                .repository(chartDetails.getRepository())
                .version(chartDetails.getVersion())
                .chartLink(chartDetails.getChartLink()).build();
    }
}
