package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EphemeralEnvironmentResponse {
    private String id;
    private String name;
    private String description;
    private UserResponse owner;
    private LocalDateTime expiryOn;
}
