package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.PipelineTemplateRequest;
import com.swadeshitech.prodhub.dto.PipelineTemplateResponse;
import com.swadeshitech.prodhub.entity.PipelineTemplate;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.PipelineTemplateType;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.PipelineTemplateService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PipelineTemplateServiceImpl implements PipelineTemplateService {

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    WriteTransactionService writeTransactionService;

    @Override
    public void createPipelineTemplate(PipelineTemplateRequest request) {

        PipelineTemplate pipelineTemplate = PipelineTemplate.builder()
                .name(request.getName())
                .version(request.getVersion())
                .pipelineTemplateType(request.getPipelineTemplateType() != null ? PipelineTemplateType.valueOf(request.getPipelineTemplateType()) : null)
                .stages(generatePipelineTemplateSteps(request.getStages()))
                .isActive(true)
                .build();

        writeTransactionService.savePipelineTemplateToRepository(pipelineTemplate);
    }

    @Override
    public PipelineTemplateResponse getPipelineTemplateDetails(String id, String version) {
        List<PipelineTemplate> pipelineTemplates = readTransactionService.findByDynamicOrFilters(Map.of(
                "version", version, "isActive", true, "_id", new ObjectId(id)),
                PipelineTemplate.class);
        if(pipelineTemplates.isEmpty()) {
            log.error("Pipeline templates could be fetched with version {}", version);
            throw new CustomException(ErrorCode.PIPELINE_TEMPLATE_COULD_NOT_BE_FOUND);
        }

        return PipelineTemplateResponse.mapEntityToDTO(pipelineTemplates.getFirst());
    }

    @Override
    public PipelineTemplateResponse getPipelineTemplateDetails(String id) {
        List<PipelineTemplate> pipelineTemplates = readTransactionService.findByDynamicOrFilters(Map.of(
                        "isActive", true, "_id", new ObjectId(id)),
                PipelineTemplate.class);
        if(pipelineTemplates.isEmpty()) {
            log.error("Pipeline templates could be fetched with id {}", id);
            throw new CustomException(ErrorCode.PIPELINE_TEMPLATE_COULD_NOT_BE_FOUND);
        }

        return PipelineTemplateResponse.mapEntityToDTO(pipelineTemplates.getFirst());
    }

    @Override
    public PipelineTemplate getPipelineTemplateByVersion(String version) {

        Page<PipelineTemplate> pipelineTemplates = readTransactionService.findByDynamicOrFiltersPaginated(
                Map.of("version", version,
                        "isActive", true),
                PipelineTemplate.class,
                0,
                1,
                "version",
                Sort.Direction.DESC);
        if(pipelineTemplates.isEmpty()) {
            log.error("Pipeline templates could be fetched with version {}", version);
            throw new CustomException(ErrorCode.PIPELINE_TEMPLATE_COULD_NOT_BE_FOUND);
        }

        return pipelineTemplates.getContent().getFirst();
    }

    private List<PipelineTemplate.StageDefinition> generatePipelineTemplateSteps(List<PipelineTemplateRequest.PipelineTemplateStageRequest> stages) {
        List<PipelineTemplate.StageDefinition> stageDefinitions = new ArrayList<>();
        for(PipelineTemplateRequest.PipelineTemplateStageRequest stageDefinition : stages) {
            stageDefinitions.add(PipelineTemplate.StageDefinition.builder()
                            .name(stageDefinition.getName())
                            .order(stageDefinition.getOrder())
                            .templateName(stageDefinition.getTemplateName())
                            .stopOnFailure(stageDefinition.isStopOnFailure())
                            .version(stageDefinition.getVersion())
                    .build());
        }
        return stageDefinitions;
    }

    @Override
    public List<DropdownDTO> getAllPipelineTemplatesForDropdown() {
        List<PipelineTemplate> pipelineTemplates = readTransactionService.findByDynamicOrFilters(
                Map.of("isActive", true), PipelineTemplate.class);
        return pipelineTemplates.stream()
                .map(template -> new DropdownDTO(template.getId(), template.getName()))
                .collect(Collectors.toList());
    }
}
