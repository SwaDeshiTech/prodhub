package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.SCMDetailsResponse;
import com.swadeshitech.prodhub.dto.SCMRegisterRequest;
import com.swadeshitech.prodhub.dto.SCMRegisterResponse;
import com.swadeshitech.prodhub.dto.SCMResponse;
import com.swadeshitech.prodhub.entity.Constants;
import com.swadeshitech.prodhub.entity.SCM;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.SCMService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SCMServiceImpl implements SCMService {

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Override
    public SCMRegisterResponse registerSCM(SCMRegisterRequest registerRequest) {

        SCM scm = new SCM();
        scm.setName(registerRequest.getName());
        scm.setDescription(registerRequest.getDescription());
        scm.setMetaData(registerRequest.getMetaData());
        scm.setActive(true);

        writeTransactionService.saveSCMToRepository(scm);

        return convertToSCMRegisterResponse(scm);
    }

    @Override
    public List<SCMResponse> scmList() {
        Constants constants = readTransactionService.getConstantByName("scm");
        List<SCMResponse> scmResponses = new ArrayList<>();
        for (String provider : constants.getValues()) {
            scmResponses.add(SCMResponse.builder().name(provider).id(provider).description("SCM Provider")
                    .location("/dashboard/connect/onboarding/scm/" + provider).isActive(true).build());
        }
        return scmResponses;
    }

    @Override
    public List<SCMResponse> registeredSCMList() {

        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);

        List<SCM> scms = readTransactionService.findSCMDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(scms)) {
            log.error("No active SCMs found");
            throw new CustomException(ErrorCode.SCM_NOT_FOUND);
        }

        List<SCMResponse> scmResponses = new ArrayList<>();
        for (SCM scm : scms) {
            scmResponses.add(convertToSCMResponse(scm));
        }

        return scmResponses;
    }

    @Override
    public SCMDetailsResponse getSCMDetails(String id) {

        Map<String, Object> filters = new HashMap<>();
        ObjectId objectId = new ObjectId(id);
        filters.put("_id", objectId);
        filters.put("isActive", true);

        List<SCM> scms = readTransactionService.findSCMDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(scms)) {
            log.error("SCM not found for id: {}", id);
            throw new CustomException(ErrorCode.SCM_NOT_FOUND);
        }

        return convertToSCMDetailsResponse(scms.get(0));
    }

    @Override
    public String deleteSCM(String id) {
        writeTransactionService.removeSCMFromRepository(id);
        log.info("SCM with id: {} deleted successfully", id);
        return "SCM deleted successfully with id: " + id;
    }

    private SCMRegisterResponse convertToSCMRegisterResponse(SCM scm) {

        return SCMRegisterResponse.builder().id(scm.getId()).name(scm.getName()).isActive(scm.isActive())
                .createdTime(scm.getCreatedTime()).createdBy(scm.getCreatedBy())
                .lastModifiedTime(scm.getLastModifiedTime()).lastModifiedBy(scm.getLastModifiedBy()).build();
    }

    private SCMResponse convertToSCMResponse(SCM scm) {
        return SCMResponse.builder().id(scm.getId()).name(scm.getName()).description(scm.getDescription())
                .isActive(scm.isActive()).createdTime(scm.getCreatedTime()).createdBy(scm.getCreatedBy())
                .lastModifiedTime(scm.getLastModifiedTime()).lastModifiedBy(scm.getLastModifiedBy()).build();
    }

    private SCMDetailsResponse convertToSCMDetailsResponse(SCM scm) {
        return SCMDetailsResponse.builder().id(scm.getId()).name(scm.getName()).description(scm.getDescription())
                .metaData(scm.getMetaData()).isActive(scm.isActive()).createdTime(scm.getCreatedTime())
                .createdBy(scm.getCreatedBy()).lastModifiedTime(scm.getLastModifiedTime())
                .lastModifiedBy(scm.getLastModifiedBy()).build();
    }
}
