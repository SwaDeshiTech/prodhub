package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.RoleRequest;
import com.swadeshitech.prodhub.dto.RoleResponse;
import com.swadeshitech.prodhub.services.RoleService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/role")
public class Role {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public ResponseEntity<Response> role(@RequestBody RoleRequest request) {

        RoleResponse roleResponse = roleService.addRole(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully created the role")
                .response(roleResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("{/id}")
    public ResponseEntity<Response> roleDetails(@PathVariable String id) {

        RoleResponse roleResponse = roleService.getRoleDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully fetched the role details")
                .response(roleResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/dropdown")
    public ResponseEntity<Response> dropdown() {

        List<DropdownDTO> dropdownDTOs = roleService.getRoleeDropdown();

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully fetched the role dropdown")
                .response(dropdownDTOs)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
