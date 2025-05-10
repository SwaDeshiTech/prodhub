package com.swadeshitech.prodhub.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentRequest implements Serializable {
    private String name;
    private String description;
    private Set<String> headOfDepartment;
    private Set<String> teams;
}
