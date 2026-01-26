package com.swadeshitech.prodhub.services;

import org.springframework.stereotype.Component;

@Component
public interface PipelineService {

    void generatePipelineConfig();
}
