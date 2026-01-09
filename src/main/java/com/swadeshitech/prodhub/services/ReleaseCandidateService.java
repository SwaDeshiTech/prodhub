package com.swadeshitech.prodhub.services;

import java.util.List;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.PaginatedResponse;
import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.ReleaseCandidateRequest;
import com.swadeshitech.prodhub.dto.ReleaseCandidateResponse;

@Component
public interface ReleaseCandidateService {

    ReleaseCandidateResponse createReleaseCandidate(ReleaseCandidateRequest request);

    ReleaseCandidateResponse getReleaseCandidateById(String id);

    ReleaseCandidateResponse updateReleaseCandidate(String id, ReleaseCandidateRequest request);

    void deleteReleaseCandidate(String id);

    PaginatedResponse<ReleaseCandidateResponse> getAllReleaseCandidates(String ephemeralEnvironment, Integer page, Integer size, String sortBy, String order);

    ReleaseCandidateResponse syncStatus(String buildId, String forceSync);

    List<DropdownDTO> getDropdownCertifiable(String applicationId);

    ReleaseCandidateResponse certifyRelaseCandidateForProduction(String id);
}