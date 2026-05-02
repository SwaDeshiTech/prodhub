package com.swadeshitech.prodhub.service.impl;

import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.entity.Department;
import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.entity.FeatureFlag;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.repository.RoleRepository;
import com.swadeshitech.prodhub.repository.TeamRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.service.AuthService;
import com.swadeshitech.prodhub.service.FeatureFlagService;
import com.swadeshitech.prodhub.service.UserApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private UserApprovalService userApprovalService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Response internalLogin(Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Check if username/password login is enabled
        if (!featureFlagService.isFeatureEnabled("admin_username_password_enabled")) {
            return Response.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("Username/password login is disabled")
                    .build();
        }

        Optional<User> userOptional = userRepository.findByUserName(username);
        if (userOptional.isEmpty()) {
            return Response.builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .message("Invalid credentials")
                    .build();
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Response.builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .message("Invalid credentials")
                    .build();
        }

        // Check if user approval is required
        if (featureFlagService.isFeatureEnabled("user_approval_required")) {
            boolean isApproved = userApprovalService.isUserApproved(user.getId());
            if (!isApproved) {
                return Response.builder()
                        .httpStatus(HttpStatus.FORBIDDEN)
                        .message("User approval pending")
                        .response(Map.of("redirect", "/access-pending"))
                        .build();
            }
        }

        if (!user.isActive()) {
            return Response.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("User account is disabled")
                    .build();
        }

        // Return user details for token generation by gateway
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("uuid", user.getId());
        userResponse.put("name", user.getName());
        userResponse.put("userName", user.getUserName());
        userResponse.put("emailId", user.getEmailId());

        return Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Credentials verified successfully")
                .response(userResponse)
                .build();
    }

    @Override
    public Response internalSignup(Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String email = userData.get("email");
        String name = userData.get("name");

        // Check if username/password signup is enabled
        if (!featureFlagService.isFeatureEnabled("admin_username_password_enabled")) {
            return Response.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("Username/password signup is disabled")
                    .build();
        }

        // Check if username already exists
        if (userRepository.findByUserName(username).isPresent()) {
            return Response.builder()
                    .httpStatus(HttpStatus.CONFLICT)
                    .message("Username already exists")
                    .build();
        }

        // Check if email already exists
        if (userRepository.findByEmailId(email).isPresent()) {
            return Response.builder()
                    .httpStatus(HttpStatus.CONFLICT)
                    .message("Email already exists")
                    .build();
        }

        // Create new user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setUserName(username);
        user.setEmailId(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);
        user.setCreatedBy("system");
        user.setLastModifiedBy("system");

        // Set default role from feature flag
        Optional<FeatureFlag> defaultRoleFlag = featureFlagService.getFeatureFlagByKey("user_default_role");
        if (defaultRoleFlag.isPresent()) {
            String roleName = defaultRoleFlag.get().getDefaultValue();
            Optional<Role> defaultRole = roleRepository.findByName(roleName);
            if (defaultRole.isPresent()) {
                user.setRoles(java.util.Set.of(defaultRole.get()));
            }
        }

        // Set default team from feature flag
        Optional<FeatureFlag> defaultTeamFlag = featureFlagService.getFeatureFlagByKey("user_default_team");
        if (defaultTeamFlag.isPresent()) {
            String teamName = defaultTeamFlag.get().getDefaultValue();
            Optional<Team> defaultTeam = teamRepository.findByName(teamName);
            if (defaultTeam.isPresent()) {
                user.setTeams(java.util.Set.of(defaultTeam.get()));
            }
        }

        // Set default department from feature flag
        Optional<FeatureFlag> defaultDeptFlag = featureFlagService.getFeatureFlagByKey("user_default_department");
        if (defaultDeptFlag.isPresent()) {
            String deptName = defaultDeptFlag.get().getDefaultValue();
            Optional<Department> defaultDept = departmentRepository.findByName(deptName);
            if (defaultDept.isPresent()) {
                user.setDepartments(java.util.Set.of(defaultDept.get()));
            }
        }

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("uuid", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("userName", savedUser.getUserName());
        response.put("emailId", savedUser.getEmailId());
        response.put("message", "Signup successful");

        return Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Signup successful")
                .response(response)
                .build();
    }
}
