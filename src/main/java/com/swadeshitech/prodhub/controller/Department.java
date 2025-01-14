package com.swadeshitech.prodhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.DepartmentRequest;
import com.swadeshitech.prodhub.dto.DepartmentResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.DepartmentService;

@RestController
@RequestMapping("/department")
public class Department {
    
    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/{id}")
    public ResponseEntity<Response> department(@PathVariable("id") String uuid) {
        
        DepartmentResponse departmentResponse = departmentService.getDepartmentDetail(uuid);
        
        Response response = Response.builder()
            .httpStatus(HttpStatus.OK)
            .message("Department detail has been fetched successfully")
            .response(departmentResponse)
            .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Response> department(@RequestBody DepartmentRequest departmentRequest) {
        
        DepartmentResponse departmentResponse = departmentService.addDepartment(departmentRequest);

        Response response = Response.builder()
            .httpStatus(HttpStatus.CREATED)
            .message("Department has been created")
            .response(departmentResponse)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
