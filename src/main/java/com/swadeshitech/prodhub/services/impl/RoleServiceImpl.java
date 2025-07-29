package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.RoleRequest;
import com.swadeshitech.prodhub.dto.RoleResponse;
import com.swadeshitech.prodhub.dto.UserRoleRequest;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.RoleService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    WriteTransactionService writeTransactionService;

    @Autowired
    ReadTransactionService readTransactionService;

    @Override
    public RoleResponse addRole(RoleRequest request) {

        Role role = new Role();
        role.setActive(true);
        role.setDefault(request.isDefault());
        role.setName(request.getName());

        writeTransactionService.saveRoleToRepository(role);

        return mapEntityToDTO(role);
    }

    @Override
    public RoleResponse getRoleDetails(String id) {

        Map<String, Object> filters = new HashMap<>();
        filters.put("id", id);

        List<Role> roles = readTransactionService.findRoleDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(roles)) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        return mapEntityToDTO(roles.get(0));
    }

    @Override
    public List<DropdownDTO> getRoleeDropdown() {

        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);

        List<Role> roles = readTransactionService.findRoleDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(roles)) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        List<DropdownDTO> dropdownDTOs = new ArrayList<>();

        for (Role role : roles) {
            dropdownDTOs.add(DropdownDTO.builder().key(role.getName()).value(role.getId()).build());
        }

        return dropdownDTOs;
    }

    @Override
    public List<RoleResponse> getUserRoleDetails(String uuid) {

        Map<String, Object> filters = new HashMap<>();
        filters.put("uuid", uuid);

        List<User> userDetails = readTransactionService.findUserDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(userDetails.get(0).getRoles())) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        List<RoleResponse> output = new ArrayList<>();
        for (Role role : userDetails.get(0).getRoles()) {
            output.add(mapEntityToDTO(role));
        }
        return output;
    }

    private RoleResponse mapEntityToDTO(Role role) {
        return RoleResponse.builder().id(role.getId()).isDefault(role.isDefault()).name(role.getName())
                .createdBy(role.getCreatedBy()).createdTime(role.getCreatedTime())
                .lastModifiedBy(role.getLastModifiedBy()).lastModifiedTime(role.getLastModifiedTime()).build();
    }

    @Override
    public List<RoleResponse> updateRoles(String uuid, UserRoleRequest request) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("uuid", uuid);

        List<User> userDetails = readTransactionService.findUserDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(userDetails)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<ObjectId> roleIds = new ArrayList<>();
        for (String roleId : request.getRoles()) {
            if (ObjectId.isValid(roleId)) {
                roleIds.add(new ObjectId(roleId));
            } else {
                throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
            }
        }

        Map<String, Object> roleFilters = new HashMap<>();
        roleFilters.put("_id", roleIds);

        List<Role> roles = readTransactionService.findRoleDetailsByFilters(roleFilters);
        if (CollectionUtils.isEmpty(roles)) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        Set<Role> updatedRoles = new HashSet<>();
        for (Role role : roles) {
            if (role.isActive()) {
                updatedRoles.add(role);
            }
        }

        User user = userDetails.get(0);
        user.setRoles(updatedRoles);

        writeTransactionService.saveUserToRepository(user);

        List<RoleResponse> roleResponses = new ArrayList<>();
        for (Role role : updatedRoles) {
            roleResponses.add(mapEntityToDTO(role));

        }
        return roleResponses;
    }
}
