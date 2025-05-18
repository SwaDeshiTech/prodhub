package com.swadeshitech.prodhub.dto;

import lombok.Data;

@Data
public class EphemeralEnvironmentApplicationResponse {
    private String ephemeralEnvironmentName;
    private String name;
    private String owner;
}
