package com.swadeshitech.prodhub.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PipelineExecutionRequest {
    Map<String, String> metaData;
    private String pipelineTemplateName;
    private String metaDataID;
}
