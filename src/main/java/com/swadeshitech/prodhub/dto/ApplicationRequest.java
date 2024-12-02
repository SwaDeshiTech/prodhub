package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class ApplicationRequest {
    
    private String name;
    private String description;
    private Long teamId;
    private Long departmentId;
    private Set<MetaDataRequest> profiles;
}
