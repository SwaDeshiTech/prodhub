package com.swadeshitech.prodhub.services.impl;

import java.util.*;

import com.swadeshitech.prodhub.config.AuditorContextHolder;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import com.swadeshitech.prodhub.utils.Base64Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class OnboardingServiceImpl implements OnboardingService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MetaDataRepository metaDataRepository;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ReadTransactionService readTransactionService;

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

        Metadata referencedProfile = null;
        if(Objects.nonNull(request.getProfile()) && StringUtils.hasText(request.getProfile().getReferencedProfileId())) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("_id", new ObjectId(request.getProfile().getReferencedProfileId()));
            List<Metadata> optionalReferencedProfile = readTransactionService.findMetaDataByFilters(filters);
            if (optionalReferencedProfile.isEmpty()) {
                throw new CustomException(ErrorCode.METADATA_PROFILE_REFERENCED_NOT_FOUND);
            }
            referencedProfile = optionalReferencedProfile.getFirst();
        }

        Metadata metadata = new Metadata();

        metadata.setActive(true);
        metadata.setName(request.getProfile().getName());
        metadata.setProfileType(request.getProfile().getProfileType());
        metadata.setDescription(request.getProfile().getDescription());
        metadata.setApplication(aOptional.get());
        metadata.setReferencedProfile(referencedProfile);
        metadata.setData(Base64Util.generateBase64Encoded(request.getProfile().getData()));

        writeTransactionService.saveMetaDataToRepository(metadata);

        if (aOptional.get().getProfiles() == null) {
            aOptional.get().setProfiles(new HashSet<>());
        }

        aOptional.get().getProfiles().add(metadata);

        String currentAuditor = AuditorContextHolder.get();
        AuditorContextHolder.set(request.getInitiatedBy());

        writeTransactionService.saveApplicationToRepository(aOptional.get());

        AuditorContextHolder.clear();
        AuditorContextHolder.set(currentAuditor);

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
        response.setDescription(metadata.getDescription());
        response.setReferencedProfileId(
                metadata.getReferencedProfile() != null ? metadata.getReferencedProfile().getId() : null);

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

        Metadata referencedProfile = null;

        if(Objects.nonNull(request.getProfile()) && StringUtils.hasText(request.getProfile().getReferencedProfileId())) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("_id", new ObjectId(request.getProfile().getReferencedProfileId()));
            List<Metadata> optionalReferencedProfile = readTransactionService.findMetaDataByFilters(filters);
            if (optionalReferencedProfile.isEmpty()) {
                throw new CustomException(ErrorCode.METADATA_PROFILE_REFERENCED_NOT_FOUND);
            }
            referencedProfile = optionalReferencedProfile.getFirst();
        }

        Metadata metadata = optionalMetaData.get();

        metadata.setActive(request.getProfile().isActive());
        metadata.setDescription(request.getProfile().getDescription());
        metadata.setData(Base64Util.generateBase64Encoded(request.getProfile().getData()));
        metadata.setReferencedProfile(referencedProfile);

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
