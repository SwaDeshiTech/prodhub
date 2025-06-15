package com.swadeshitech.prodhub.services.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.dto.ApplicationProfileRequest;
import com.swadeshitech.prodhub.dto.ApplicationProfileResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.MetaDataResponse;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ApplicationRepository;
import com.swadeshitech.prodhub.repository.MetaDataRepository;
import com.swadeshitech.prodhub.services.OnboardingService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OnboardingServiceImpl implements OnboardingService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MetaDataRepository metaDataRepository;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Override
    public Set<DropdownDTO> getProfilesForDropdown(String onboardingType, String applicationId) {

        Optional<Application> aOptional = applicationRepository.findById(applicationId);
        if (aOptional.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Set<DropdownDTO> dropdownDTOs = new HashSet<>();

        for (Metadata metadata : aOptional.get().getProfiles()) {
            if (metadata.getProfileType().getValue().equals(onboardingType)) {
                dropdownDTOs.add(DropdownDTO.builder()
                        .key(metadata.getId())
                        .value(metadata.getName())
                        .build());
            }
        }

        return dropdownDTOs;
    }

    @Override
    public ApplicationProfileResponse onboardProfile(ApplicationProfileRequest request) {

        Optional<Application> aOptional = applicationRepository.findById(request.getApplicationId());
        if (aOptional.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Optional<Metadata> optionalMetaData = metaDataRepository.findByName(request.getProfile().getName());
        if (optionalMetaData.isPresent()) {
            throw new CustomException(ErrorCode.METADATA_PROFILE_ALREADY_EXISTS);
        }

        Metadata metadata = new Metadata();

        metadata.setActive(true);
        metadata.setName(request.getProfile().getName());
        metadata.setProfileType(request.getProfile().getProfileType());
        metadata.setApplication(aOptional.get());
        metadata.setData(Base64Util.generateBase64Encoded(request.getProfile().getData()));

        writeTransactionService.saveMetaDataToRepository(metadata);

        if (aOptional.get().getProfiles() == null) {
            aOptional.get().setProfiles(new HashSet<>());
        }

        aOptional.get().getProfiles().add(metadata);
        writeTransactionService.saveApplicationToRepository(aOptional.get());

        MetaDataResponse dataResponse = mapEntityDataResponse(metadata);

        ApplicationProfileResponse applicationProfileResponse = new ApplicationProfileResponse();

        applicationProfileResponse.setApplicationId(request.getApplicationId());
        applicationProfileResponse.setProfile(dataResponse);

        return applicationProfileResponse;
    }

    private MetaDataResponse mapEntityDataResponse(Metadata metadata) {

        MetaDataResponse response = new MetaDataResponse();

        response.setActive(metadata.isActive());
        response.setName(metadata.getName());
        response.setData(Base64Util.convertToPlainText(metadata.getData()));
        response.setProfileType(metadata.getProfileType());

        return response;
    }

    @Override
    public MetaDataResponse updateProfile(ApplicationProfileRequest request) {

        Optional<Application> aOptional = applicationRepository.findById(request.getApplicationId());
        if (aOptional.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Optional<Metadata> optionalMetaData = metaDataRepository.findById(request.getProfile().getId());
        if (optionalMetaData.isEmpty()) {
            throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
        }

        Metadata metadata = optionalMetaData.get();

        metadata.setActive(request.getProfile().isActive());
        metadata.setData(Base64Util.generateBase64Encoded(request.getProfile().getData()));

        writeTransactionService.saveMetaDataToRepository(metadata);

        return mapEntityDataResponse(metadata);
    }

    @Override
    public ApplicationProfileResponse getProfileDetails(String profileId) {

        Optional<Metadata> meOptional = metaDataRepository.findById(profileId);
        if (meOptional.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        ApplicationProfileResponse applicationProfileResponse = new ApplicationProfileResponse();
        applicationProfileResponse.setApplicationId(meOptional.get().getApplication().getId());
        MetaDataResponse metaDataResponse = mapEntityDataResponse(meOptional.get());
        applicationProfileResponse.setProfile(metaDataResponse);

        return applicationProfileResponse;
    }

}
