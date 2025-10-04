package com.swadeshitech.prodhub.services.impl;

import java.util.*;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.entity.*;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ApplicationRepository;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.repository.TeamRepository;
import com.swadeshitech.prodhub.services.ApplicationService;
import com.swadeshitech.prodhub.utils.Base64Util;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamServiceImpl teamService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    MetadataServiceImpl metadataService;

    @Autowired
    ReadTransactionService readTransactionService;

    @Override
    public ApplicationResponse addApplication(ApplicationRequest applicationRequest) {

        Application application = modelMapper.map(applicationRequest, Application.class);
        if (Objects.isNull(application)) {
            log.error("application request could not be converted", applicationRequest);
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Optional<Team> team = teamRepository.findById(applicationRequest.getTeamId());
        if (team.isEmpty()) {
            log.error("team could not be found", team);
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        Optional<Department> department = departmentRepository.findById(applicationRequest.getDepartmentId());
        if (department.isEmpty()) {
            log.error("department could not be found", team);
            throw new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        if (!isValidateRequest(team.get(), department.get())) {
            log.error("department and team are not valid");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        application.setDepartment(department.get());
        application.setTeam(team.get());
        application.setActive(true);
        application.setProfiles(new HashSet<>());

        saveApplicationToRepository(application);

        ApplicationResponse applicationResponse = modelMapper.map(application, ApplicationResponse.class);
        return applicationResponse;
    }

    @Override
    public ApplicationResponse getApplicationDetail(String id) {

        if (StringUtils.isEmpty(id)) {
            log.error("application name is empty/null");
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Optional<Application> application = applicationRepository.findById(id);
        if (application.isEmpty()) {
            log.error("application could not be found", id);
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        decodeProfleMetaData(application.get());

        return mapEntityToDTO(application.get());
    }

    protected Application saveApplicationToRepository(Application application) {
        try {
            return applicationRepository.save(application);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    private void decodeProfleMetaData(Application application) {
        if (Objects.isNull(application.getProfiles())) {
            return;
        }
        for (Metadata metadata : application.getProfiles()) {
            metadata.setData(Base64Util.convertToPlainText(metadata.getData()));
        }
    }

    @Override
    public List<DropdownDTO> getAllApplicationsDropdown() {
        List<Application> applications = applicationRepository.findAll();
        if (applications.isEmpty()) {
            log.error("No applications found");
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }
        return applications.stream().map(application -> {
            DropdownDTO dropdown = new DropdownDTO();
            dropdown.setKey(application.getId());
            dropdown.setValue(application.getName());
            return dropdown;
        }).toList();
    }

    @Override
    public List<DropdownDTO> getApplicationDropdownByUserAccess() {

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if(org.apache.commons.lang3.StringUtils.isBlank(userId)){
            log.error("User ID could not be found in the logs");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        List<User> users = readTransactionService.findUserDetailsByFilters(Map.of("uuid", userId));
        if(CollectionUtils.isEmpty(users)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        User user = users.getFirst();
        Set<Team> teams = user.getTeams();

        if (CollectionUtils.isEmpty(teams)) {
            log.error("Team is not attached to the user");
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        List<DropdownDTO> dropdownValues = new ArrayList<>();

        for(Team team : teams) {
            Set<Application> applicationSet = team.getApplications();
            if (!CollectionUtils.isEmpty(applicationSet)) {
                for(Application application : applicationSet) {
                    dropdownValues.add(DropdownDTO.builder()
                            .value(application.getName())
                            .key(application.getId())
                            .build());
                }
            }
        }
        return dropdownValues;
    }

    private boolean isValidateRequest(Team team, Department department) {
        for (Department departmentItr : team.getDepartments()) {
            if (departmentItr.equals(department)) {
                return true;
            }
        }
        return false;
    }

    private ApplicationResponse mapEntityToDTO(Application application) {

        Set<MetaDataResponse> metaDataResponseSet = new HashSet<>();
        TeamResponse teamResponse = teamService.mapEntityToDTO(application.getTeam());

        for(Metadata metadata : application.getProfiles()) {
            metaDataResponseSet.add(metadataService.mapToMetaDataResponse(metadata));
        }

        return ApplicationResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .description(application.getDescription())
                .profiles(metaDataResponseSet)
                .isActive(application.isActive())
                .team(teamResponse)
                .departmentResponse(teamResponse.getDepartments().getFirst())
                .createdBy(application.getCreatedBy())
                .createdTime(application.getCreatedTime())
                .lastModifiedBy(application.getLastModifiedBy())
                .lastModifiedTime(application.getLastModifiedTime())
                .build();
    }
}
