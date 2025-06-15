package com.swadeshitech.prodhub.services;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.ApplicationProfileRequest;
import com.swadeshitech.prodhub.dto.ApplicationProfileResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.MetaDataResponse;

@Component
public interface OnboardingService {

    Set<DropdownDTO> getProfilesForDropdown(String onboardingType, String applicationId);

    ApplicationProfileResponse onboardProfile(ApplicationProfileRequest request);

    MetaDataResponse updateProfile(ApplicationProfileRequest request);

    ApplicationProfileResponse getProfileDetails(String profileId);
}
