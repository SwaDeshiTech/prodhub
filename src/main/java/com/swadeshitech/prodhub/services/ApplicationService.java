package com.swadeshitech.prodhub.services;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.ApplicationRequest;
import com.swadeshitech.prodhub.dto.ApplicationResponse;

@Component
public interface ApplicationService {

    public ApplicationResponse addApplication(ApplicationRequest serviceRequest);

    public ApplicationResponse getApplicationDetail(String name);
}