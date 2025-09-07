package com.swadeshitech.prodhub.integration.vault;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class VaultRequest {
    private String credentialPath;
    private Map<String, Object> data;
}
