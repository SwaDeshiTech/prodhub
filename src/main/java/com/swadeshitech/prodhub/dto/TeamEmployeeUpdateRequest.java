package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class TeamEmployeeUpdateRequest {
    private String description;
    private Set<String> employeeList;
    private Set<String> managerList;// List of email IDs to set as team employees
}