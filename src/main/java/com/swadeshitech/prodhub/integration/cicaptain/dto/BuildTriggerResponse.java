package com.swadeshitech.prodhub.integration.cicaptain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BuildTriggerResponse (
        @JsonProperty("data") Data data,
        @JsonProperty("message") String message
) {
    public record Data(
            @JsonProperty("build_id") String buildId,
            @JsonProperty("queue_id") String queueId,
            @JsonProperty("status") String status,
            @JsonProperty("url") String url,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") String timestamp
    ) {}
}
