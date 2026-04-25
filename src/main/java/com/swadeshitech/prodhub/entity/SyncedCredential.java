package com.swadeshitech.prodhub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncedCredential implements Serializable {

    private String buildProviderId;

    private String credentialId;

    private LocalDateTime syncedAt;
}
