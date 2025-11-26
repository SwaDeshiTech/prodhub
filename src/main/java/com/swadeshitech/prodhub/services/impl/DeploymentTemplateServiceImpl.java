package com.swadeshitech.prodhub.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swadeshitech.prodhub.dto.DeploymentTemplateRequest;
import com.swadeshitech.prodhub.dto.DeploymentTemplateResponse;
import com.swadeshitech.prodhub.entity.DeploymentTemplate;
import com.swadeshitech.prodhub.services.DeploymentTemplateService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DeploymentTemplateServiceImpl implements DeploymentTemplateService {

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public DeploymentTemplateResponse createDeploymentTemplate(DeploymentTemplateRequest request) {

        DeploymentTemplate deploymentTemplate = DeploymentTemplate.builder()
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .version(request.getVersion())
                .steps(generateDeploymentStep(request.getStepRequests()))
                .build();

        deploymentTemplate = writeTransactionService.saveDeploymentTemplate(deploymentTemplate);

        return mapDTOToEntity(deploymentTemplate);
    }

    private List<DeploymentTemplate.DeploymentStep> generateDeploymentStep(List<DeploymentTemplateRequest.DeploymentTemplateStepRequest> stepRequests) {

        List<DeploymentTemplate.DeploymentStep> steps = new ArrayList<>();

        for(DeploymentTemplateRequest.DeploymentTemplateStepRequest itr : stepRequests) {
            steps.add(DeploymentTemplate.DeploymentStep.builder()
                            .order(itr.getOrder())
                            .wait(itr.isWait())
                            .stepName(itr.getStepName())
                            .timeoutSeconds(itr.getTimeoutSeconds())
                            .params(itr.getParams())
                            .chartDetails(generateChartDetails(itr.getChartDetails()))
                    .build());
        }

        return steps;
    }

    private DeploymentTemplate.DeploymentStep.ChartDetails generateChartDetails(DeploymentTemplateRequest.DeploymentTemplateStepRequest.ChartDetailsRequest chartDetailsRequest) {
        return DeploymentTemplate.DeploymentStep.ChartDetails.builder()
                .chartName(chartDetailsRequest.getChartName())
                .repository(chartDetailsRequest.getRepository())
                .version(chartDetailsRequest.getVersion())
                .chartLink(chartDetailsRequest.getChartLink())
                .build();
    }

    private DeploymentTemplateResponse mapDTOToEntity(DeploymentTemplate deploymentTemplate) {
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

    private List<DeploymentTemplateResponse.DeploymentTemplateStepResponse> generateDeploymentTemplateResponse(List<DeploymentTemplate.DeploymentStep> steps) {

        List<DeploymentTemplateResponse.DeploymentTemplateStepResponse> deploymentTemplateStepResponseList = new ArrayList<>();

        for(DeploymentTemplate.DeploymentStep step : steps) {
            deploymentTemplateStepResponseList.add(DeploymentTemplateResponse.DeploymentTemplateStepResponse.builder()
                            .stepName(step.getStepName())
                            .wait(step.isWait())
                            .timeoutSeconds(step.getTimeoutSeconds())
                            .params(step.getParams())
                            .order(step.getOrder())
                            .chartDetails(generateChartDetailsResponse(step.getChartDetails()))
                    .build());
        }
        return deploymentTemplateStepResponseList;
    }

    private DeploymentTemplateResponse.DeploymentTemplateStepResponse.ChartDetailsResponse generateChartDetailsResponse(DeploymentTemplate.DeploymentStep.ChartDetails chartDetails) {
        return DeploymentTemplateResponse.DeploymentTemplateStepResponse.ChartDetailsResponse.builder()
                .chartName(chartDetails.getChartName())
                .repository(chartDetails.getRepository())
                .version(chartDetails.getVersion())
                .chartLink(chartDetails.getChartLink()).build();
    }
}
