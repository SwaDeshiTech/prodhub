package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.DepartmentRequest;
import com.swadeshitech.prodhub.dto.DepartmentResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;

@Component
public interface DepartmentService {

    public DepartmentResponse addDepartment(DepartmentRequest departmentRequest);

    public DepartmentResponse getDepartmentDetail(String departmentUUID);

    public List<DropdownDTO> getAllDepartmentsForDropdown();

    public String updateDepartment(String departmentUUID, DepartmentRequest departmentRequest);
}