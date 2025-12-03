package com.swadeshitech.prodhub.services.impl;

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
    WriteTransactionService writeTransactionService;

    @Override
    public DeploymentTemplateResponse createDeploymentTemplate(DeploymentTemplateRequest request) {

        DeploymentTemplate deploymentTemplate = DeploymentTemplate.builder()
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .version(request.getVersion())
                .steps(generateDeploymentStep(request.getStepRequests()))
                .build();

        deploymentTemplate = writeTransactionService.saveDeploymentTemplate(deploymentTemplate);

        return DeploymentTemplateResponse.mapDTOToEntity(deploymentTemplate);
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
}
