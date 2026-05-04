package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.RoleRequest;
import com.swadeshitech.prodhub.dto.RoleResponse;
import com.swadeshitech.prodhub.dto.UserRoleRequest;
import com.swadeshitech.prodhub.services.RoleService;

@RestController
@RequestMapping("/role")
public class Role {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public ResponseEntity<Response> role(@RequestBody RoleRequest request) {

        RoleResponse roleResponse = roleService.addRole(request);

        Response response = Response.builder().httpStatus(HttpStatus.CREATED).message("Successfully created the role")
                .response(roleResponse).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/user")
    public ResponseEntity<Response> updateUserRoles(@RequestParam(name = "userId", required = false) String userId,
            @RequestBody UserRoleRequest request) {

        if (!StringUtils.hasText(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message("userId query parameter is required")
                    .response(null)
                    .build());
        }

        List<RoleResponse> roleResponse = roleService.updateRoles(userId, request);

        Response response = Response.builder().httpStatus(HttpStatus.CREATED)
                .message("Successfully updated the roles of the user").response(roleResponse).build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/details")
    public ResponseEntity<Response> getUserRoleDetails(@RequestParam(name = "userId", required = false) String userId) {

        if (!StringUtils.hasText(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message("userId query parameter is required")
                    .response(null)
                    .build());
        }

        List<RoleResponse> roleDetails = roleService.getUserRoleDetails(userId);

        Response response = Response.builder().httpStatus(HttpStatus.OK)
                .message("Successfully fetched the role details").response(roleDetails).build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Response> roleDetails(@PathVariable String id) {

        RoleResponse roleResponse = roleService.getRoleDetails(id);

        Response response = Response.builder().httpStatus(HttpStatus.OK)
                .message("Successfully fetched the role details").response(roleResponse).build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/dropdown")
    public ResponseEntity<Response> dropdown() {

        List<DropdownDTO> dropdownDTOs = roleService.getRoleeDropdown();

        Response response = Response.builder().httpStatus(HttpStatus.OK)
                .message("Successfully fetched the role dropdown").response(dropdownDTOs).build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
