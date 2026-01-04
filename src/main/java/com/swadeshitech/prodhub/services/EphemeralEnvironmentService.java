package com.swadeshitech.prodhub.services;

import java.util.List;
import java.util.Map;

import com.swadeshitech.prodhub.dto.*;
import org.springframework.stereotype.Component;

@Component
public interface EphemeralEnvironmentService {

    public EphemeralEnvironmentResponse createEphemeralEnvironment(EphemeralEnvironmentRequest request);

    public EphemeralEnvironmentResponse getEphemeralEnvironmentDetail(String id);

    public EphemeralEnvironmentApplicationResponse getEphemeralEnvironmentApplicationDetails(String id,
            String applicationId);

    public List<DropdownDTO> getEphemeralEnvironmentDropdownList();

    public PaginatedResponse<EphemeralEnvironmentResponse> getEphemeralEnvironmentList(Integer page, Integer size, String sortBy, String order);

    public EphemeralEnvironmentResponse updateEphemeralEnvironment(String environmentId,
            EphemeralEnvironmentRequest request);

    public Map<String, Object> getMetadataFromEphemeralEnvironment(String id);

    public void setUpProfiles(String ephemeralEnvironmentId, String profileType);
}
