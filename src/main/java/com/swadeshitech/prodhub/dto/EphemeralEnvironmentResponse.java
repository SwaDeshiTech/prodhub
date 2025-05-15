package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;

import com.swadeshitech.prodhub.enums.EphemeralEnvrionmentStatus;

import lombok.Data;

@Data
public class EphemeralEnvironmentResponse {
    private String id;
    private String name;
    private String owner;
    private EphemeralEnvrionmentStatus status;
    private LocalDateTime expiryOn;
    private LocalDateTime createdTime;
}
