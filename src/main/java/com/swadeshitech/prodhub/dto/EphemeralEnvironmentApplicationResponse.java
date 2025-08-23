package com.swadeshitech.prodhub.dto;

import java.util.Map;

import lombok.Data;

@Data
public class EphemeralEnvironmentApplicationResponse {
    private String ephemeralEnvironmentName;
    private Map<String, Object> applications;
}
