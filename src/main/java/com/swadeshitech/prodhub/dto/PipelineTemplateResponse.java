package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.entity.PipelineTemplate;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
public class PipelineTemplateResponse extends BaseResponse {
    private String id;
    private String name;
    private String version;
    private List<PipelineTemplateStageResponse> stages;

    @Data
    @Builder
    public static class PipelineTemplateStageResponse {
        private String name;
        private String templateName;
        private int order;
        private boolean stopOnFailure;
    }

    public static PipelineTemplateResponse mapEntityToDTO(PipelineTemplate pipelineTemplate) {
        return PipelineTemplateResponse.builder()
                .id(pipelineTemplate.getId())
                .name(pipelineTemplate.getName())
                .version(pipelineTemplate.getVersion())
                .stages(buildPipelineTemplateStageResponse(pipelineTemplate.getStages()))
                .createdBy(pipelineTemplate.getCreatedBy())
                .createdTime(pipelineTemplate.getCreatedTime())
                .lastModifiedBy(pipelineTemplate.getLastModifiedBy())
                .lastModifiedTime(pipelineTemplate.getLastModifiedTime())
                .build();
    }

    private static List<PipelineTemplateStageResponse> buildPipelineTemplateStageResponse(List<PipelineTemplate.StageDefinition> stages) {
        List<PipelineTemplateStageResponse> stageResponses = new ArrayList<>();
        for(PipelineTemplate.StageDefinition stage : stages) {
            stageResponses.add(PipelineTemplateStageResponse.builder()
                            .name(stage.getName())
                            .templateName(stage.getTemplateName())
                            .order(stage.getOrder())
                            .stopOnFailure(stage.isStopOnFailure())
                    .build());
        }
        return stageResponses;
    }
}
