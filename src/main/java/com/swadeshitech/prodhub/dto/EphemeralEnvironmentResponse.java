package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.swadeshitech.prodhub.enums.EphemeralEnvrionmentStatus;

import lombok.Data;

@Data
public class EphemeralEnvironmentResponse {
    private String id;
    private String name;
    private String owner;
    private EphemeralEnvrionmentStatus status;
    private Set<DropdownDTO> applications;
    private Set<DropdownDTO> sharedWith;
    private LocalDateTime expiryOn;
    private LocalDateTime createdTime;
}
