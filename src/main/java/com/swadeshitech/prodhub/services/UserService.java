package com.swadeshitech.prodhub.services;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.UserRequest;
import com.swadeshitech.prodhub.dto.UserResponse;
import com.swadeshitech.prodhub.dto.UserUpdateRequest;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.dto.DropdownDTO;

@Component
public interface UserService {

    public UserResponse getUserDetail(String uuid);

    public UserResponse addUser(UserRequest userRequest);

    public UserResponse updateUser(String uuid, UserUpdateRequest updateRequest);

    public List<DropdownDTO> getAllUsersForDropdown();

    public Set<Role> getUserRoles(String uuid);
}