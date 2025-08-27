package com.swadeshitech.prodhub.dto;

import java.util.List;
import lombok.Data;

@Data
public class DepartmentResponse extends BaseResponse {

    private String name;
    private String description;
    private boolean isActive;
    private List<DropdownDTO> teams;
    private List<DropdownDTO> headOfDepartment;
}
