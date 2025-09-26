package com.swadeshitech.prodhub.services.impl;

import com.swadeshitech.prodhub.dto.CodeFreezeRequest;
import com.swadeshitech.prodhub.dto.CodeFreezeResponse;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.CodeFreeze;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.CodeFreezeService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class CodeFreezeServiceImpl implements CodeFreezeService {

    @Autowired
    private ReadTransactionService readTransactionService;

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Override
    public CodeFreezeResponse createCodeFreeze(CodeFreezeRequest request) {

        CodeFreeze codeFreeze = CodeFreeze.builder()
                .startTime(request.getStartDateTime())
                .endTime(request.getEndDateTime())
                .isActive(true)
                .description(request.getDescription())
                .build();

        updateCommonFields(codeFreeze, request);

        codeFreeze = writeTransactionService.saveCodeFreezeToRepository(codeFreeze);

        return mapEntityToDTO(codeFreeze);
    }

    @Override
    public List<CodeFreezeResponse> getCodeFreezeList() {

        List<CodeFreeze> codeFreezeList = readTransactionService.findCodeFreezeByFilters(Map.of());
        if (CollectionUtils.isEmpty(codeFreezeList)) {
            log.error("Code freeze not found");
            throw new CustomException(ErrorCode.CODE_FREEZE_LIST_NOT_FOUND);
        }

        List<CodeFreezeResponse> codeFreezeResponses = new ArrayList<>();

        for (CodeFreeze codeFreeze : codeFreezeList) {
            codeFreezeResponses.add(CodeFreezeResponse.builder()
                    .id(codeFreeze.getId())
                    .description(codeFreeze.getDescription())
                    .isActive(codeFreeze.isActive())
                    .startTime(codeFreeze.getStartTime())
                    .endTime(codeFreeze.getEndTime())
                    .createdBy(codeFreeze.getCreatedBy())
                    .createdTime(codeFreeze.getCreatedTime())
                    .lastModifiedBy(codeFreeze.getLastModifiedBy())
                    .lastModifiedTime(codeFreeze.getLastModifiedTime())
                    .build());
        }

        return codeFreezeResponses;
    }

    @Override
    public CodeFreezeResponse codeFreezeDetails(String id) {

        List<CodeFreeze> codeFreezes = readTransactionService.findCodeFreezeByFilters(
                Map.of("_id", new ObjectId(id)));
        if (codeFreezes.isEmpty()) {
            log.error("No code freeze found for id: {}", id);
            throw new CustomException(ErrorCode.CODE_FREEZE_NOT_FOUND);
        }

        return mapEntityToDTO(codeFreezes.getFirst());
    }

    @Override
    public void updateCodeFreeze(String id, CodeFreezeRequest request) {

        List<CodeFreeze> codeFreezeList = readTransactionService.findCodeFreezeByFilters(Map.of("_id", id));
        if (CollectionUtils.isEmpty(codeFreezeList)) {
            log.error("Code freeze could not be found: {}", id);
            throw new CustomException(ErrorCode.CODE_FREEZE_NOT_FOUND);
        }

        CodeFreeze codeFreeze = codeFreezeList.getFirst();

        codeFreeze.setActive(request.isActive());
        codeFreeze.setDescription(request.getDescription());
        codeFreeze.setStartTime(request.getStartDateTime());
        codeFreeze.setEndTime(request.getEndDateTime());

        updateCommonFields(codeFreeze, request);

        writeTransactionService.saveCodeFreezeToRepository(codeFreeze);
    }

    private CodeFreezeResponse mapEntityToDTO(CodeFreeze codeFreeze) {

        if (Objects.isNull(codeFreeze)) {
            return null;
        }

        List<String> applications = new ArrayList<>();
        List<String> users = new ArrayList<>();

        for (Application application : codeFreeze.getApplications()) {
            applications.add(application.getName());
        }

        for (User user : codeFreeze.getApprovers()) {
            users.add(user.getName() + " (" + user.getEmailId() + ")");
        }

        return CodeFreezeResponse.builder()
                .id(codeFreeze.getId())
                .description(codeFreeze.getDescription())
                .approvers(users)
                .applications(applications)
                .isActive(codeFreeze.isActive())
                .startTime(codeFreeze.getStartTime())
                .endTime(codeFreeze.getEndTime())
                .createdBy(codeFreeze.getCreatedBy())
                .createdTime(codeFreeze.getCreatedTime())
                .lastModifiedBy(codeFreeze.getLastModifiedBy())
                .lastModifiedTime(codeFreeze.getLastModifiedTime())
                .build();
    }

    private void updateCommonFields(CodeFreeze codeFreeze, CodeFreezeRequest request) {
        List<ObjectId> applicationIdsFilter = new ArrayList<>();
        for (String id : request.getApplicationIds()) {
            applicationIdsFilter.add(new ObjectId(id));
        }

        List<User> approvers = readTransactionService.findUserDetailsByFilters(Map.of("uuid", request.getApprovers()));
        if (Objects.isNull(approvers)) {
            log.error("Approvers could not be found");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<Application> applications = readTransactionService
                .findApplicationByFilters(Map.of("_id", applicationIdsFilter));
        if (Objects.isNull(applications)) {
            log.error("Applications could not be found");
            throw new CustomException(ErrorCode.APPLICATION_LIST_NOT_FOUND);
        }

        //Team

        codeFreeze.setApprovers(approvers);
        codeFreeze.setApplications(applications);
    }

    /*public CodeFreeze isCodeFreezeEnabled(String applicationId) {

        LocalDateTime currentTime = LocalDateTime.now();

        Map<String, Object> filters = new HashMap<>();
        filters.put("startTime", currentTime);
        filters.put("endTime", currentTime);
        filters.put("applications", applicationId);


    }*/
}
