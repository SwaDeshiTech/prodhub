package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {
    private String id;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime createdTime;
    private LocalDateTime lastModifiedTime;
}
