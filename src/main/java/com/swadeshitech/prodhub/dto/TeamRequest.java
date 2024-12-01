package com.swadeshitech.prodhub.dto;

import java.util.Set;
import lombok.Data;

@Data
public class TeamRequest {
    
    private String name;
    private String description;
    private Long departmentId;
    private Set<String> employeeList;
}
