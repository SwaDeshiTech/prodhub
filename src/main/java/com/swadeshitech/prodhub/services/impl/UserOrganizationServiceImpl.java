package com.swadeshitech.prodhub.services.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.swadeshitech.prodhub.dto.UserOrganizationRequest;
import com.swadeshitech.prodhub.dto.UserOrganizationResponse;
import com.swadeshitech.prodhub.entity.Organization;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.entity.UserOrganization;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.UserOrganizationRepository;
import com.swadeshitech.prodhub.services.UserOrganizationService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserOrganizationServiceImpl implements UserOrganizationService {

    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Override
    public UserOrganizationResponse addUserToOrganization(UserOrganizationRequest request) {
        log.info("Adding user {} to organization {}", request.getUserId(), request.getOrganizationId());

        // Check if user already belongs to this organization
        if (userOrganizationRepository.existsByUserIdAndOrganizationIdAndActiveTrue(
                request.getUserId(), request.getOrganizationId())) {
            throw new CustomException(ErrorCode.USER_ORGANIZATION_ALREADY_EXISTS);
        }

        // Fetch user by email (userId field contains email in this case)
        List<User> users = readTransactionService.findUserByFilters(
                java.util.Map.of("emailId", request.getUserId()));
        if (CollectionUtils.isEmpty(users)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<Organization> organizations = readTransactionService.findOrganizationDetailsByFilters(
                java.util.Map.of("_id", new org.bson.types.ObjectId(request.getOrganizationId())));
        if (CollectionUtils.isEmpty(organizations)) {
            throw new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        // Create user-organization mapping with default role
        String defaultRoleName;
        try {
            var defaultRoles = userService.getDefaultRoles();
            if (defaultRoles == null || defaultRoles.isEmpty()) {
                log.error("No default role found in system");
                throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
            }
            defaultRoleName = defaultRoles.iterator().next().getName();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get default role from UserService", e);
            throw new CustomException(ErrorCode.ROLE_NOT_FOUND);
        }

        UserOrganization userOrganization = UserOrganization.builder()
                .userId(users.get(0).getId())
                .organizationId(request.getOrganizationId())
                .user(users.get(0))
                .organization(organizations.get(0))
                .role(defaultRoleName)
                .active(true)
                .build();

        writeTransactionService.saveUserOrganizationToRepository(userOrganization);

        log.info("User {} added to organization {} successfully", request.getUserId(), request.getOrganizationId());

        return mapToResponse(userOrganization);
    }

    @Override
    public List<UserOrganizationResponse> getOrganizationsForUser(String userId) {
        log.info("Fetching organizations for user {}", userId);

        List<UserOrganization> userOrganizations = userOrganizationRepository
                .findByUserIdAndActiveTrue(userId);

        return userOrganizations.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<UserOrganizationResponse> addUsersToOrganizationViaCsv(MultipartFile file, String organizationId) {
        log.info("Processing CSV upload for organization {}", organizationId);

        List<UserOrganizationResponse> responses = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 1) {
                    continue;
                }

                String email = data[0].trim();
                String role = data.length > 1 ? data[1].trim() : "MEMBER";

                // Find or create user by email
                List<User> users = readTransactionService.findUserByFilters(
                        java.util.Map.of("emailId", email));

                User user;
                if (CollectionUtils.isEmpty(users)) {
                    // Create new user
                    user = new User();
                    user.setUuid(UUID.randomUUID().toString());
                    user.setEmailId(email);
                    user.setName(email.split("@")[0]); // Use email prefix as name
                    user.setIsActive(true);
                    writeTransactionService.saveUserToRepository(user);
                    log.info("Created new user with email {}", email);
                } else {
                    user = users.get(0);
                }

                // Add user to organization
                if (!userOrganizationRepository.existsByUserIdAndOrganizationIdAndActiveTrue(
                        user.getId(), organizationId)) {
                    UserOrganization userOrganization = UserOrganization.builder()
                            .userId(user.getId())
                            .organizationId(organizationId)
                            .user(user)
                            .role(role)
                            .active(true)
                            .build();

                    writeTransactionService.saveUserOrganizationToRepository(userOrganization);
                    responses.add(mapToResponse(userOrganization));
                }
            }

            log.info("CSV processing completed. Added {} users to organization {}", responses.size(), organizationId);
            return responses;

        } catch (Exception e) {
            log.error("Failed to process CSV file", e);
            throw new CustomException(ErrorCode.CSV_UPLOAD_FAILED);
        }
    }

    @Override
    public boolean canUserCreateOrganization(String userId) {
        log.info("Checking if user {} can create organization", userId);

        List<UserOrganization> userOrganizations = userOrganizationRepository
                .findByUserIdAndActiveTrue(userId);

        // User can create organization only if they don't belong to any organization
        return CollectionUtils.isEmpty(userOrganizations);
    }

    @Override
    public UserOrganizationResponse linkCreatorToOrganization(String userId, String organizationId) {
        log.info("Linking creator {} to organization {}", userId, organizationId);

        // Check if user already belongs to this organization
        if (userOrganizationRepository.existsByUserIdAndOrganizationIdAndActiveTrue(userId, organizationId)) {
            // Already linked, return existing
            List<UserOrganization> userOrganizations = userOrganizationRepository
                    .findByUserIdAndActiveTrue(userId);
            return mapToResponse(userOrganizations.get(0));
        }

        // Fetch user and organization
        List<User> users = readTransactionService.findUserByFilters(
                java.util.Map.of("_id", new org.bson.types.ObjectId(userId)));
        if (CollectionUtils.isEmpty(users)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<Organization> organizations = readTransactionService.findOrganizationDetailsByFilters(
                java.util.Map.of("_id", new org.bson.types.ObjectId(organizationId)));
        if (CollectionUtils.isEmpty(organizations)) {
            throw new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        // Create user-organization mapping with OWNER role
        UserOrganization userOrganization = UserOrganization.builder()
                .userId(userId)
                .organizationId(organizationId)
                .user(users.get(0))
                .organization(organizations.get(0))
                .role("OWNER")
                .active(true)
                .build();

        writeTransactionService.saveUserOrganizationToRepository(userOrganization);

        log.info("Creator {} linked to organization {} successfully", userId, organizationId);

        return mapToResponse(userOrganization);
    }

    private UserOrganizationResponse mapToResponse(UserOrganization userOrganization) {
        return UserOrganizationResponse.builder()
                .userId(userOrganization.getUserId())
                .organizationId(userOrganization.getOrganizationId())
                .organizationName(userOrganization.getOrganization() != null 
                        ? userOrganization.getOrganization().getName() 
                        : null)
                .role(userOrganization.getRole())
                .createdTime(userOrganization.getCreatedTime())
                .lastModifiedTime(userOrganization.getLastModifiedTime())
                .build();
    }

    @Override
    public List<UserOrganizationResponse> getOrganizationMembers(String organizationId) {
        log.info("Fetching members for organization {}", organizationId);

        List<UserOrganization> userOrganizations = userOrganizationRepository
                .findByOrganizationIdAndActiveTrue(organizationId);

        return userOrganizations.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}
