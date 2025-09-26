package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
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

@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    @Autowired
    private ReadTransactionService readTransactionService;

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
        filters.put("application", applications.get(0));

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
            metadataList.forEach(
                    metadata -> dropdownDTOs.add(new DropdownDTO(metadata.getId().toString(), metadata.getName())));
        } else {
            for (Metadata metadata : metadataList) {
                var referencedProfile = metadata.getReferencedProfile();
                if (!ObjectUtils.isEmpty(referencedProfile)) {
                    String label = metadata.getName() + " (" + metadata.getProfileType() + " Profile) " + " -> "
                            + referencedProfile.getName() + " (" + referencedProfile.getProfileType() + " Profile)";
                    dropdownDTOs.add(new DropdownDTO(metadata.getId().toString(), label));
                }
            }
        }

        return dropdownDTOs;
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
