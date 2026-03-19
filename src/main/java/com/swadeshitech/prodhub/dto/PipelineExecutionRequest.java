package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PipelineExecutionRequest {
    Map<String, String> metaData;
    private String pipelineTemplateName;
    private String metaDataID;
}
