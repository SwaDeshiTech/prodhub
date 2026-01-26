package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.PipelineTemplateRequest;
import com.swadeshitech.prodhub.dto.PipelineTemplateResponse;
import com.swadeshitech.prodhub.entity.PipelineTemplate;
import org.springframework.stereotype.Component;

@Component
public interface PipelineTemplateService {

    void createPipelineTemplate(PipelineTemplateRequest request);

    PipelineTemplateResponse getPipelineTemplateDetails(String id, String version);

    PipelineTemplate getPipelineTemplateByVersion(String version);
}
