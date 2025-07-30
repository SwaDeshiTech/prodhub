package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.SCMDetailsResponse;
import com.swadeshitech.prodhub.dto.SCMRegisterRequest;
import com.swadeshitech.prodhub.dto.SCMRegisterResponse;
import com.swadeshitech.prodhub.dto.SCMResponse;

@Component
public interface SCMService {

    public SCMRegisterResponse registerSCM(SCMRegisterRequest registerRequest);

    public List<SCMResponse> scmList();

    public List<SCMResponse> registeredSCMList();

    public SCMDetailsResponse getSCMDetails(String id);

    public String deleteSCM(String id);
}