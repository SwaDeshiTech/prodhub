package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TokenResponse extends BaseResponse {
    private String tokenId;
    private String token;
    private String description;
    private Integer expiryDays;
    private LocalDateTime expiresAt;
    private boolean active;
    private LocalDateTime lastUsedAt;
}
