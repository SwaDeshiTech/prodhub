package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.TemplateRequest;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import org.springframework.stereotype.Component;

@Component
public interface TemplateService {
    TemplateResponse createTemplate(TemplateRequest request);
}
