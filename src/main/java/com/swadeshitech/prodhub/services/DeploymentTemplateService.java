package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.DeploymentTemplateRequest;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import org.springframework.stereotype.Component;

@Component
public interface DeploymentTemplateService {
    TemplateResponse createDeploymentTemplate(DeploymentTemplateRequest request);
}
