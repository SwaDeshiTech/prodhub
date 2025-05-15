package com.swadeshitech.prodhub.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.dto.ApplicationRequest;
import com.swadeshitech.prodhub.dto.ApplicationResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.Department;
import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.enums.ProfileType;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ApplicationRepository;
import com.swadeshitech.prodhub.repository.DepartmentRepository;
import com.swadeshitech.prodhub.repository.TeamRepository;
import com.swadeshitech.prodhub.services.ApplicationService;
import com.swadeshitech.prodhub.utils.Base64Util;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

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
    ModelMapper modelMapper;

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

        // generateBase64String(application);
        // setProfilesToActive(application);

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

        ApplicationResponse applicationResponse = modelMapper.map(application.get(), ApplicationResponse.class);
        return applicationResponse;
    }

    private Application saveApplicationToRepository(Application application) {
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

    private void generateBase64String(Application application) {
        for (Metadata metadata : application.getProfiles()) {
            metadata.setData(Base64Util.generateBase64Encoded(metadata.getData()));
        }
    }

    private void decodeProfleMetaData(Application application) {
        for (Metadata metadata : application.getProfiles()) {
            metadata.setData(Base64Util.convertToPlainText(metadata.getData()));
        }
    }

    private void setProfilesToActive(Application application) {
        for (Metadata metadata : application.getProfiles()) {
            metadata.setActive(true);
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

    private boolean isValidateRequest(Team team, Department department) {
        for (Department departmentItr : team.getDepartments()) {
            if (departmentItr.equals(department)) {
                return true;
            }
        }
        return false;
    }
}
