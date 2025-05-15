package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class EphemeralEnvironmentRequest {

    private String name;
    private String description;
    private int expiryDuration;
    private Set<String> applications;
}
