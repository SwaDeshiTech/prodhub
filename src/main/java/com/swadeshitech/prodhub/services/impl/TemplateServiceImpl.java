package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.TemplateRequest;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import com.swadeshitech.prodhub.entity.Template;
import com.swadeshitech.prodhub.enums.StepExecutionStatus;
import com.swadeshitech.prodhub.repository.TemplateRepository;
import com.swadeshitech.prodhub.services.TemplateService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    TemplateRepository templateRepository;

    @Override
    public TemplateResponse createTemplate(TemplateRequest request) {

        Template template = Template.builder()
                .templateName(request.getTemplateName())
                .description(request.getDescription())
                .version(request.getVersion())
                .steps(generateStep(request.getStepRequests()))
                .build();

        template = writeTransactionService.saveTemplate(template);

        return TemplateResponse.mapDTOToEntity(template);
    }

    @Override
    public List<TemplateResponse> getAllTemplates() {
        List<Template> templates = templateRepository.findAll();
        List<TemplateResponse> templateResponses = new ArrayList<>();
        for (Template template : templates) {
            templateResponses.add(TemplateResponse.mapDTOToEntity(template));
        }
        return templateResponses;
    }

    @Override
    public TemplateResponse getTemplateDetails(String id) {
        Optional<Template> template = templateRepository.findById(id);
        if (template.isEmpty()) {
            log.error("Template not found for id: {}", id);
            throw new RuntimeException("Template not found");
        }
        return TemplateResponse.mapDTOToEntity(template.get());
    }

    private Set<Template.Step> generateStep(List<TemplateRequest.TemplateStepRequest> stepRequests) {
        Set<Template.Step> steps = new HashSet<>();
        for (TemplateRequest.TemplateStepRequest itr : stepRequests) {
            steps.add(Template.Step.builder()
                    .order(itr.getOrder())
                    .status(StepExecutionStatus.CREATED)
                    .wait(itr.isWait())
                    .stepName(itr.getStepName())
                    .timeoutSeconds(itr.getTimeoutSeconds())
                    .skipStep(itr.isSkipStep())
                    .params(generateTemplateStepParam(itr.getParams()))
                    .chartDetails(generateChartDetails(itr.getChartDetails()))
                    .build());
        }
        return steps;
    }

    private Template.Step.ChartDetails generateChartDetails(TemplateRequest.TemplateStepRequest.ChartDetailsRequest chartDetailsRequest) {
        if(chartDetailsRequest == null) {
            return null;
        }
        return Template.Step.ChartDetails.builder()
                .chartName(chartDetailsRequest.getChartName())
                .repository(chartDetailsRequest.getRepository())
                .version(chartDetailsRequest.getVersion())
                .chartLink(chartDetailsRequest.getChartLink())
                .build();
    }

    private Map<String, Template.Step.TemplateStepParam> generateTemplateStepParam(Map<String, TemplateRequest.TemplateStepRequest.TemplateStepRequestParam>
                                                                                           templateStepRequestParam) {
        Map<String, Template.Step.TemplateStepParam> params = new HashMap<>();
        if (templateStepRequestParam != null && !templateStepRequestParam.isEmpty()) {
            for (Map.Entry<String, TemplateRequest.TemplateStepRequest.TemplateStepRequestParam> itr : templateStepRequestParam.entrySet()) {
                params.put(itr.getKey(), Template.Step.TemplateStepParam.builder()
                        .affectedKey(itr.getValue().getAffectedKey())
                        .displayName(itr.getValue().getDisplayName())
                        .value("")
                        .build());
            }
        }
        return params;
    }
}
