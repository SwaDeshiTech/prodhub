package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.ApplicationRequest;
import com.swadeshitech.prodhub.dto.ApplicationResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;

@Component
public interface ApplicationService {

    public ApplicationResponse addApplication(ApplicationRequest serviceRequest);

    public ApplicationResponse getApplicationDetail(String name);

    public List<DropdownDTO> getAllApplicationsDropdown();

    public List<DropdownDTO> getApplicationDropdownByUserAccess();
}