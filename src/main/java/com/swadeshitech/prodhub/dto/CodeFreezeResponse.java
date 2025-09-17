package com.swadeshitech.prodhub.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
public class CodeFreezeResponse extends BaseResponse {
    private String id;
    private String description;
    private boolean isActive;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> applications;
    private List<String> approvers;
}
