package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class EphemeralEnvironmentRequest {
    
    private String name;
    private String description;
    private LocalDateTime expiryOn;
    private Set<String> applications;
}
