package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.OrganizationRegisterRequest;
import com.swadeshitech.prodhub.dto.OrganizationRegisterResponse;
import com.swadeshitech.prodhub.entity.Organization;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.services.OrganizationService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.transaction.write.WriteTransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private WriteTransactionService writeTransactionService;

    @Autowired
    private ReadTransactionService readTransactionService;

    @Override
    public OrganizationRegisterResponse registerOrganization(OrganizationRegisterRequest request) {

        log.info("Registering organization with request: {}", request);

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setActive(true);

        writeTransactionService.saveOrganizationToRepository(organization);

        // Implementation logic for registering an organization
        return mapEntityToDTO(organization);
    }

    @Override
    public List<DropdownDTO> getOrganizationList() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);

        List<Organization> organizations = readTransactionService.findOrganizationDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(organizations)) {
            log.info("Organization list is empty");
            throw new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        List<DropdownDTO> response = new ArrayList<>();
        for (Organization organization : organizations) {
            response.add(DropdownDTO.builder()
                    .key(organization.getId())
                    .value(organization.getName())
                    .build());
        }
        return response;
    }

    private OrganizationRegisterResponse mapEntityToDTO(Organization organization) {
        return OrganizationRegisterResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .active(organization.isActive())
                .createdBy(organization.getCreatedBy())
                .createdTime(organization.getCreatedTime())
                .lastModifiedBy(organization.getLastModifiedBy())
                .lastModifiedTime(organization.getLastModifiedTime())
                .build();
    }

    @Override
    public OrganizationRegisterResponse getOrganizationDetails(String id) {
        log.info("Fetching organization details for ID: {}", id);

        Map<String, Object> filters = new HashMap<>();
        filters.put("isActive", true);
        filters.put("_id", new ObjectId(id));

        List<Organization> organizations = readTransactionService.findOrganizationDetailsByFilters(filters);
        if (CollectionUtils.isEmpty(organizations)) {
            log.info("Organization list is empty");
            throw new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        return mapEntityToDTO(organizations.get(0));
    }
}
