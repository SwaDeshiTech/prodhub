package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swadeshitech.prodhub.constant.Constants;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.MetaDataResponse;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.MetadataService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Override
    public MetaDataResponse getMetadataDetails(String id) {

        Map<String, Object> filters = Map.of("_id", new ObjectId(id));
        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(filters);
        if (metadataList.isEmpty()) {
            log.error("No metadata found for id: {}", id);
            throw new CustomException(ErrorCode.METADATA_PROFILE_NOT_FOUND);
        }
        return mapToMetaDataResponse(metadataList.get(0));
    }

    @Override
    public List<MetaDataResponse> getAllMetadataDetails(String applicationId) {

        Map<String, Object> filters = Map.of("application", new ObjectId(applicationId));

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(filters);
        if (metadataList.isEmpty()) {
            log.error("No metadata found for applicationId: {}", applicationId);
            throw new CustomException(ErrorCode.METADATA_PROFILE_LIST_NOT_FOUND);
        }

        return metadataList.stream()
                .map(this::mapToMetaDataResponse)
                .toList();
    }

    @Override
    public List<DropdownDTO> getAllMetadataNames(String applicationId, String type) {

        List<Application> applications = readTransactionService.findApplicationByFilters(
                Map.of("_id", new ObjectId(applicationId)));
        if (applications.isEmpty()) {
            log.error("No application found for id: {}", applicationId);
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        Map<String, Object> filters = new HashMap<>();
        filters.put("application", applications.getFirst());

        if (type != null) {
            filters.put("profileType", type);
        }

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(filters);
        if (metadataList.isEmpty()) {
            log.error("No metadata found for applicationId: {} and type: {}", applicationId, type);
            throw new CustomException(ErrorCode.METADATA_PROFILE_LIST_NOT_FOUND);
        }

        List<DropdownDTO> dropdownDTOs = new ArrayList<>();

        if (type != null) {
            for(Metadata metadata : metadataList) {
                if(metadata.getName().split(Constants.CLONE_METADATA_DELIMITER).length == 1) {
                    dropdownDTOs.add(new DropdownDTO(metadata.getId(), metadata.getName()));
                }
            }
        } else {
            for (Metadata metadata : metadataList) {
                var referencedProfile = metadata.getReferencedProfile();
                if (!ObjectUtils.isEmpty(referencedProfile)) {
                    String label = metadata.getName() + " (" + metadata.getProfileType() + " Profile) " + " -> "
                            + referencedProfile.getName() + " (" + referencedProfile.getProfileType() + " Profile)";
                    dropdownDTOs.add(new DropdownDTO(metadata.getId(), label));
                }
            }
        }

        return dropdownDTOs;
    }

    @Override
    public Metadata cloneProfile(String sourceProfileId, String cloneProfileName) {
        return fetchAndCloneMetadataProfile(sourceProfileId, cloneProfileName);
    }

    private Metadata fetchAndCloneMetadataProfile(String metaDataProfileId, String cloneProfileName) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("_id", new ObjectId(metaDataProfileId));

        List<Metadata> metadataList = readTransactionService.findMetaDataByFilters(filters);
        if (metadataList.isEmpty()) {
            log.error("No metadata found for metaDataProfileId: {}", metaDataProfileId);
            throw new CustomException(ErrorCode.METADATA_PROFILE_LIST_NOT_FOUND);
        }

        Metadata metadata = metadataList.getFirst();
        Metadata clonedProfile = new Metadata();

        BeanUtils.copyProperties(metadata, clonedProfile, "id");
        if(StringUtils.hasText(cloneProfileName)) {
            clonedProfile.setName(cloneProfileName);
        }
        clonedProfile = writeTransactionService.saveMetadataToRepository(clonedProfile);

        return clonedProfile;
    }

    protected MetaDataResponse mapToMetaDataResponse(Metadata metadata) {
        return MetaDataResponse.builder()
                .id(metadata.getId())
                .name(metadata.getName())
                .isActive(metadata.isActive())
                .data(metadata.getData())
                .profileType(metadata.getProfileType())
                .referencedProfileId(
                        metadata.getReferencedProfile() != null ? metadata.getReferencedProfile().getId() : null)
                .description(metadata.getDescription())
                .createdBy(metadata.getCreatedBy())
                .createdTime(metadata.getCreatedTime())
                .lastModifiedBy(metadata.getLastModifiedBy())
                .lastModifiedTime(metadata.getLastModifiedTime())
                .build();
    }

}
