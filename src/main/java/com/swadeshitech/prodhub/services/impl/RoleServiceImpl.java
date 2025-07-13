package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.RoleRequest;
import com.swadeshitech.prodhub.dto.RoleResponse;
import com.swadeshitech.prodhub.entity.Role;
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
            dropdownDTOs.add(DropdownDTO
                    .builder()
                    .key(role.getName())
                    .value(role.getId())
                    .build());
        }

        return dropdownDTOs;
    }

    private RoleResponse mapEntityToDTO(Role role) {
        return RoleResponse.builder()
                .isDefault(role.isDefault())
                .name(role.getName())
                .build();
    }
}
