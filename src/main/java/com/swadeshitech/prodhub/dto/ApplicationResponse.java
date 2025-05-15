package com.swadeshitech.prodhub.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationResponse {
    private String id;
    private String name;
    private String description;
    private TeamResponse team;
    private DepartmentResponse departmentResponse;
    private boolean isActive;
    private Set<MetaDataRequest> profiles;
}
