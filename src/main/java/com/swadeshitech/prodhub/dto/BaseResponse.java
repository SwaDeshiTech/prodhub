package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BaseResponse {
    private String id;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;
}
