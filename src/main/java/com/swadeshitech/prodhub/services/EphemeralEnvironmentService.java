package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentRequest;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentResponse;

@Component
public interface EphemeralEnvironmentService {

    public EphemeralEnvironmentResponse createEphemeralEnvironment(EphemeralEnvironmentRequest request);

    public EphemeralEnvironmentResponse getEphemeralEnvironmentDetail(String id);

    public EphemeralEnvironmentResponse getEphemeralEnvironmentApplicationDetails(String id, String applicationId);

    public List<DropdownDTO> getEphemeralEnvironmentDropdownList();

    public List<EphemeralEnvironmentResponse> getEphemeralEnvironmentList();

    public EphemeralEnvironmentResponse updateEphemeralEnvironment(String environmentId,
            EphemeralEnvironmentRequest request);
}
