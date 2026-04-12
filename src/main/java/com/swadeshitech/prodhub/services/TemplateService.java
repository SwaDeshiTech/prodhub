package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.TemplateRequest;
import com.swadeshitech.prodhub.dto.TemplateResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TemplateService {
    TemplateResponse createTemplate(TemplateRequest request);
    List<TemplateResponse> getAllTemplates();
}
