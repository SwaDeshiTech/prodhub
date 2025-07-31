package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.OrganizationRegisterRequest;
import com.swadeshitech.prodhub.dto.OrganizationRegisterResponse;

@Component
public interface OrganizationService {

    public OrganizationRegisterResponse registerOrganization(OrganizationRegisterRequest request);

    public List<DropdownDTO> getOrganizationList();

    public OrganizationRegisterResponse getOrganizationDetails(String id);
}
