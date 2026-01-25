package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.DeploymentTemplateRequest;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import com.swadeshitech.prodhub.entity.Template;
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
    public TemplateResponse createDeploymentTemplate(DeploymentTemplateRequest request) {

        Template template = Template.builder()
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .version(request.getVersion())
                .steps(generateDeploymentStep(request.getStepRequests()))
                .build();

        template = writeTransactionService.saveDeploymentTemplate(template);

        return TemplateResponse.mapDTOToEntity(template);
    }

    private List<Template.Step> generateDeploymentStep(List<DeploymentTemplateRequest.DeploymentTemplateStepRequest> stepRequests) {

        List<Template.Step> steps = new ArrayList<>();

        for(DeploymentTemplateRequest.DeploymentTemplateStepRequest itr : stepRequests) {
            steps.add(Template.Step.builder()
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

    private Template.Step.ChartDetails generateChartDetails(DeploymentTemplateRequest.DeploymentTemplateStepRequest.ChartDetailsRequest chartDetailsRequest) {
        return Template.Step.ChartDetails.builder()
                .chartName(chartDetailsRequest.getChartName())
                .repository(chartDetailsRequest.getRepository())
                .version(chartDetailsRequest.getVersion())
                .chartLink(chartDetailsRequest.getChartLink())
                .build();
    }
}
