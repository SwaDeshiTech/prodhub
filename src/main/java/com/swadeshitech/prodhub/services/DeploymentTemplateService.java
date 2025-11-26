package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.DeploymentTemplateRequest;
import com.swadeshitech.prodhub.dto.DeploymentTemplateResponse;
import org.springframework.stereotype.Component;

@Component
public interface DeploymentTemplateService {
    DeploymentTemplateResponse createDeploymentTemplate(DeploymentTemplateRequest request);
}
