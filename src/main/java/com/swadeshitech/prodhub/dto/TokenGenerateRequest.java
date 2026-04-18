package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenGenerateRequest {
    private String description;
    private Integer expiryDays;
}
