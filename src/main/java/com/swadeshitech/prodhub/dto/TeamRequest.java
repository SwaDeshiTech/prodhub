package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class TeamRequest {
    
    private String name;
    private String description;
    private String departmentName;
    private Set<String> employeeList;
}
