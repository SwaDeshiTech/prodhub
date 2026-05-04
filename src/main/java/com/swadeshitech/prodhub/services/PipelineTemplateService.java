package com.swadeshitech.prodhub.services;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.PipelineTemplateRequest;
import com.swadeshitech.prodhub.dto.PipelineTemplateResponse;
import com.swadeshitech.prodhub.entity.PipelineTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PipelineTemplateService {

    void createPipelineTemplate(PipelineTemplateRequest request);

    PipelineTemplateResponse getPipelineTemplateDetails(String id, String version);

    PipelineTemplateResponse getPipelineTemplateDetails(String id);

    PipelineTemplate getPipelineTemplateByVersion(String version);

    List<DropdownDTO> getAllPipelineTemplatesForDropdown();
}
