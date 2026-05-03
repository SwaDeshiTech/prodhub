package com.swadeshitech.prodhub.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.swadeshitech.prodhub.config.ContextHolder;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.MappingException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.UserRequest;
import com.swadeshitech.prodhub.dto.UserResponse;
import com.swadeshitech.prodhub.dto.UserUpdateRequest;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.entity.Department;
import com.swadeshitech.prodhub.entity.FeatureFlag;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.repository.RoleRepository;
import com.swadeshitech.prodhub.repository.TeamRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.service.FeatureFlagService;
import com.swadeshitech.prodhub.service.UserApprovalService;
import com.swadeshitech.prodhub.services.UserService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.utils.UserContextUtil;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserApprovalService userApprovalService;

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

        Set<Department> departments = user.get().getDepartments();
        if (departments != null && !departments.isEmpty()) {
            try {
                Set<String> names = departments.stream()
                        .map(Department::getName)
                        .collect(Collectors.toSet());
                userResponse.setDepartments(names);
            } catch (NullPointerException e) {
                log.error("Failed to map departments to userResponse: Data is missing", e);
                userResponse.setDepartments(Collections.emptySet());
            } catch (Exception e) {
                log.error("Unexpected error during department mapping", e);
            }
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
        user.setTeams(getDefaultTeams());
        user.setDepartments(getDefaultDepartments());

        saveUserDetailToRepository(user);

        // Automatically create pending approval entry
        userApprovalService.createOrUpdatePendingApproval(user);

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

        return users.getFirst().getRoles();
    }

    @Override
    public Set<Role> getDefaultRoles() {

        // Try to get default role from feature flag
        Optional<FeatureFlag> defaultRoleFlag = featureFlagService.getFeatureFlagByKey("user_default_role");
        if (defaultRoleFlag.isPresent() && Strings.isNotBlank(defaultRoleFlag.get().getDefaultValue())) {
            String roleName = defaultRoleFlag.get().getDefaultValue();
            Optional<Role> defaultRole = roleRepository.findByName(roleName);
            if (defaultRole.isPresent()) {
                return Set.of(defaultRole.get());
            }
        }

        // Fallback to isDefault=true roles
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

    public Set<Team> getDefaultTeams() {
        Optional<FeatureFlag> defaultTeamFlag = featureFlagService.getFeatureFlagByKey("user_default_team");
        if (defaultTeamFlag.isPresent() && Strings.isNotBlank(defaultTeamFlag.get().getDefaultValue())) {
            String teamName = defaultTeamFlag.get().getDefaultValue();
            Optional<Team> defaultTeam = teamRepository.findByName(teamName);
            if (defaultTeam.isPresent()) {
                return Set.of(defaultTeam.get());
            }
        }
        return new HashSet<>();
    }

    public Set<Department> getDefaultDepartments() {
        Optional<FeatureFlag> defaultDeptFlag = featureFlagService.getFeatureFlagByKey("user_default_department");
        if (defaultDeptFlag.isPresent() && Strings.isNotBlank(defaultDeptFlag.get().getDefaultValue())) {
            String deptName = defaultDeptFlag.get().getDefaultValue();
            Optional<Department> defaultDept = departmentRepository.findByName(deptName);
            if (defaultDept.isPresent()) {
                return Set.of(defaultDept.get());
            }
        }
        return new HashSet<>();
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

    public User extractUserFromContext() {
        String uuid = (String) ContextHolder.getContext("uuid");
        if (ObjectUtils.isEmpty(uuid)) {
            log.error("UUID is null or empty");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Map<String, Object> userFilters = new HashMap<>();
        userFilters.put("uuid", uuid);

        List<User> userOption = readTransactionService.findUserDetailsByFilters(userFilters);
        if (CollectionUtils.isEmpty(userOption)) {
            log.error("User with UUID: {} not found", uuid);
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return userOption.getFirst();
    }

}
