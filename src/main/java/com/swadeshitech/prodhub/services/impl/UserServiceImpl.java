package com.swadeshitech.prodhub.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.UserRequest;
import com.swadeshitech.prodhub.dto.UserResponse;
import com.swadeshitech.prodhub.dto.UserUpdateRequest;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.entity.Department;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.services.UserService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.utils.UserContextUtil;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Override
    public UserResponse getUserDetail(String uuid) {
        if (StringUtils.isEmpty(uuid)) {
            log.error("user uuid is empty/null");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Optional<User> user = userRepository.findByUuid(uuid);
        if (user.isEmpty()) {
            log.error("user not found");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        UserResponse userResponse = modelMapper.map(user.get(), UserResponse.class);

        // Handle null teams collection
        Set<Team> teams = user.get().getTeams();
        if (teams != null) {
            userResponse.setTeams(teams.stream().map(Team::getName).collect(Collectors.toSet()));
        } else {
            userResponse.setTeams(new HashSet<>());
        }

        // Handle null departments collection
        Set<Department> departments = user.get().getDepartments();
        if (departments != null) {
            userResponse.setDepartments(departments.stream().map(Department::getName).collect(Collectors.toSet()));
        } else {
            userResponse.setDepartments(new HashSet<>());
        }

        userResponse.setCreatedBy(user.get().getCreatedBy());
        userResponse.setCreatedTime(user.get().getCreatedTime());
        userResponse.setLastModifiedBy(user.get().getLastModifiedBy());
        userResponse.setLastModifiedTime(user.get().getLastModifiedTime());

        return userResponse;
    }

    @Override
    public UserResponse addUser(UserRequest userRequest) {

        Optional<User> userByEmail = userRepository.findByEmailId(userRequest.getEmailId());
        if (userByEmail.isPresent()) {
            return modelMapper.map(userByEmail.get(), UserResponse.class);
        }

        User user = modelMapper.map(userRequest, User.class);
        if (Objects.isNull(user)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        user.setIsActive(Boolean.TRUE);
        user.setRoles(getDefaultRoles());

        saveUserDetailToRepository(user);

        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public UserResponse updateUser(String uuid, UserUpdateRequest updateRequest) {
        if (StringUtils.isEmpty(uuid)) {
            log.error("user uuid is empty/null");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Optional<User> userOptional = userRepository.findByUuid(uuid);
        if (userOptional.isEmpty()) {
            log.error("user not found");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        // Update phone number if provided
        if (Strings.isNotBlank(updateRequest.getPhoneNumber())) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        } else {
            log.error("Phone number is empty");
            throw new CustomException(ErrorCode.USER_DETAILS_MISSING);
        }

        saveUserDetailToRepository(user);

        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public List<DropdownDTO> getAllUsersForDropdown() {
        List<User> users = userRepository.findAll();
        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (Objects.isNull(userId)) {
            log.error("user id is not present");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }
        return users.stream()
                // .filter(user -> !userId.equals(user.getUuid()))
                .map(user -> new DropdownDTO(
                        user.getUuid(),
                        user.getName() + " (" + user.getEmailId() + ")"))
                .collect(Collectors.toList());
    }

    @Override
    public Set<Role> getUserRoles(String uuid) {

        Map<String, Object> filters = new HashMap<>();
        filters.put("uuid", uuid);

        List<User> users = readTransactionService.findUserDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(users) && users.size() > 1) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        return users.get(0).getRoles();
    }

    private Set<Role> getDefaultRoles() {

        Map<String, Object> filters = new HashMap<>();
        filters.put("isDefault", true);

        List<Role> roles = readTransactionService.findRoleDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(roles)) {
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        Set<Role> defaultRoles = new HashSet<>();
        for (Role role : roles) {
            defaultRoles.add(role);
        }

        return defaultRoles;
    }

    private User saveUserDetailToRepository(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

}
