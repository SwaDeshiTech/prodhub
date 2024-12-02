package com.swadeshitech.prodhub.dto;

import java.util.Set;
import lombok.Data;

@Data
public class ApplicationResponse {
    private Long id;
    private String name;
    private String description;
    private TeamResponse team;
    private DepartmentResponse departmentResponse;
    private boolean isActive;
    private Set<MetaDataRequest> profiles;
}
