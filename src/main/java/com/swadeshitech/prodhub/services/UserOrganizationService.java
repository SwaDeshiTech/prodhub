package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.swadeshitech.prodhub.dto.UserOrganizationRequest;
import com.swadeshitech.prodhub.dto.UserOrganizationResponse;

@Component
public interface UserOrganizationService {

    UserOrganizationResponse addUserToOrganization(UserOrganizationRequest request);

    List<UserOrganizationResponse> getOrganizationsForUser(String userId);

    List<UserOrganizationResponse> addUsersToOrganizationViaCsv(MultipartFile file, String organizationId);

    boolean canUserCreateOrganization(String userId);

    UserOrganizationResponse linkCreatorToOrganization(String userId, String organizationId);

    List<UserOrganizationResponse> getOrganizationMembers(String organizationId);
}
