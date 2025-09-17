package com.swadeshitech.prodhub.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CodeFreezeRequest {
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<String> applicationIds;
    private List<String> approvers;
    private boolean isActive;
}
