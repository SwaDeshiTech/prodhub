package com.swadeshitech.prodhub.integration.cicaptain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BuildStatusResponse(
        @JsonProperty("build_id") String buildId,
        @JsonProperty("status") String status,
        @JsonProperty("duration") long duration,
        @JsonProperty("start_time") String startTime,
        @JsonProperty("finish_time") String finishTime
) {}
