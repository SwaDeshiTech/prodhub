package com.swadeshitech.prodhub.services.impl;

import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.swadeshitech.prodhub.dto.DepartmentRequest;
import com.swadeshitech.prodhub.dto.DepartmentResponse;
import com.swadeshitech.prodhub.entity.Department;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.services.DepartmentService;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.PersistenceException;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    ModelMapper modelMapper;
    
    @Override
    public DepartmentResponse addDepartment(DepartmentRequest departmentRequest) {

        Department department = modelMapper.map(departmentRequest, Department.class);
        if (Objects.isNull(department)) {
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }
        
        department.setActive(Boolean.TRUE);

        saveDepartmentDetailToRepository(department);
        DepartmentResponse departmentResponse = modelMapper.map(department, DepartmentResponse.class);

        return departmentResponse;
    }

    @Override
    public DepartmentResponse getDepartmentDetail(String departmentUUID) {

        if (StringUtils.isEmpty(departmentUUID)) {
            log.error("department uuid is empty/null");
            throw new CustomException(ErrorCode.DEPARTMENT_UUID_NOT_FOUND);
        }

        Optional<Department> department = departmentRepository.findById(departmentUUID);
        if (department.isEmpty()) {
            log.error("department not found");
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        DepartmentResponse departmentResponse = modelMapper.map(department.get(), DepartmentResponse.class);

        return departmentResponse;
    }
    
    private Department saveDepartmentDetailToRepository(Department department) {
        try {
            return departmentRepository.save(department);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (PersistenceException ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }
    
}
