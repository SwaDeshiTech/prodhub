package com.swadeshitech.prodhub.dto;

import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class EphemeralEnvironmentRequest {

    private String name;
    private int expiryDuration;
    private Set<String> applications;
    private Set<String> sharedWith;
    private Map<String, Map<String, String>> metadata;
}
