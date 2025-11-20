package com.swadeshitech.prodhub.services;

import java.util.List;

import com.swadeshitech.prodhub.entity.Metadata;
import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.MetaDataResponse;

@Component
public interface MetadataService {

    MetaDataResponse getMetadataDetails(String id);

    List<MetaDataResponse> getAllMetadataDetails(String applicationId);

    List<DropdownDTO> getAllMetadataNames(String applicationId, String type);

    Metadata cloneProfile(String sourceProfileId, String cloneProfileName);
}
