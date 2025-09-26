package com.swadeshitech.prodhub.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ApplicationResponse extends BaseResponse {
    private String id;
    private String name;
    private String description;
    private TeamResponse team;
    private DepartmentResponse departmentResponse;
    private boolean isActive;
    private Set<MetaDataResponse> profiles;
}
