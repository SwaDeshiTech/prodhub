package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class ApplicationRequest {
    
    private String name;
    private String description;
    private Long teamId;
    private String departmentId;
    private Set<MetaDataRequest> profiles;
}
