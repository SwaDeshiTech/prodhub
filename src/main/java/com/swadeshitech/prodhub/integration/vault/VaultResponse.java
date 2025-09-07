package com.swadeshitech.prodhub.integration.vault;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class VaultResponse {
    private String credentialPath;
    private Map<String, Object> data;
}
