package com.swadeshitech.prodhub.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class EphemeralEnvironmentApplicationResponse {
    private String ephemeralEnvironmentName;
    private Map<String, List<MetaDataResponse>> applications;
}
