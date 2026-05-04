package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.RoleRequest;
import com.swadeshitech.prodhub.dto.RoleResponse;
import com.swadeshitech.prodhub.dto.UserRoleRequest;

@Component
public interface RoleService {

    RoleResponse addRole(RoleRequest request);

    RoleResponse getRoleDetails(String id);

    List<DropdownDTO> getRoleeDropdown();

    List<RoleResponse> getUserRoleDetails(String userId);

    List<RoleResponse> updateRoles(String userId, UserRoleRequest request);
}
