package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class TeamRequest {

    private String name;
    private String description;
    private String departmentId;
    private Set<String> employeeList;
    private Set<String> managerList;
    private Set<String> applications;
}
