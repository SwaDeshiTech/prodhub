package com.swadeshitech.prodhub.provider;

import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.integration.cicaptain.dto.BuildTriggerResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public interface BuildProvider {
    BuildTriggerResponse triggerBuild(PipelineExecution pipelineExecution, Metadata buildProfile,
                                      Map<String, String> values);
}
