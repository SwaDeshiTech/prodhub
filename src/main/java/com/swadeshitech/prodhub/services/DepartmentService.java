package com.swadeshitech.prodhub.services;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.DepartmentRequest;
import com.swadeshitech.prodhub.dto.DepartmentResponse;

@Component
public interface DepartmentService {

    public DepartmentResponse addDepartment(DepartmentRequest departmentRequest);

    public DepartmentResponse getDepartmentDetail(String departmentUUID);
}