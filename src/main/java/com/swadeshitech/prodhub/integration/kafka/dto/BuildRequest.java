package com.swadeshitech.prodhub.integration.kafka.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuildRequest {
    private String releaseCandidateId;
}
