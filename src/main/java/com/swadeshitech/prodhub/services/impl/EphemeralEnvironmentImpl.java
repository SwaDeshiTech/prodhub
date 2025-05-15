package com.swadeshitech.prodhub.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentRequest;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentResponse;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.EphemeralEnvironment;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.EphemeralEnvrionmentStatus;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ApplicationRepository;
import com.swadeshitech.prodhub.repository.EphemeralEnvironmentRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.services.EphemeralEnvironmentService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class EphemeralEnvironmentImpl implements EphemeralEnvironmentService {

    @Autowired
    private EphemeralEnvironmentRepository environmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public EphemeralEnvironmentResponse createEphemeralEnvironment(EphemeralEnvironmentRequest request) {

        EphemeralEnvironment environment = modelMapper.map(request, EphemeralEnvironment.class);

        if (Objects.isNull(environment)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (Objects.isNull(userId)) {
            log.error("user id is not present");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        environment.setStatus(EphemeralEnvrionmentStatus.CREATED);
        environment.setExpiryOn(LocalDateTime.now().plusDays(request.getExpiryDuration()));

        setOwnerDetail(environment, userId);

        setApplications(environment, request.getApplications());

        saveEphemeralEnvironmentToRepository(environment);

        return modelMapper.map(environment, EphemeralEnvironmentResponse.class);
    }

    @Override
    public EphemeralEnvironmentResponse getEphemeralEnvironmentDetail(String id) {

        if (!StringUtils.hasText(id)) {
            log.error("ephemeral environment id is not present");
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_ID_NOT_FOUND);
        }

        Optional<EphemeralEnvironment> environment = environmentRepository.findById(id);
        if (environment.isEmpty()) {
            log.error("ephemeral environment could not be found", id);
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_ID_NOT_FOUND);
        }

        return mapEntityToResponse(environment.get());
    }

    private EphemeralEnvironment saveEphemeralEnvironmentToRepository(EphemeralEnvironment environment) {
        try {
            log.info("null", environment);
            return environmentRepository.save(environment);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (Exception ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    private void setOwnerDetail(EphemeralEnvironment environment, String uuId) {

        Optional<User> user = userRepository.findByUuid(uuId);
        if (user.isEmpty()) {
            log.error("user could not be found", uuId);
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        environment.setOwner(user.get());
    }

    private void setApplications(EphemeralEnvironment environment, Set<String> applications) {
        List<Application> applicationList = applicationRepository.findAllById(applications);

        if (applicationList.isEmpty()) {
            log.error("applications could not be found", applications);
            throw new CustomException(ErrorCode.APPLICATION_LIST_NOT_FOUND);
        }

        Set<Application> applicationSet = Set.copyOf(applicationList);

        environment.setApplications(applicationSet);
    }

    @Override
    public List<DropdownDTO> getEphemeralEnvironmentDropdownList() {

        List<EphemeralEnvironment> environments = environmentRepository.findAll();
        if (environments.isEmpty()) {
            log.error("ephemeral environment list is empty");
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_LIST_NOT_FOUND);
        }

        List<DropdownDTO> dropdownDTOs = new ArrayList<>();

        for (EphemeralEnvironment environment : environments) {
            dropdownDTOs.add(DropdownDTO.builder().key(environment.getName()).value(environment.getId()).build());
        }

        return dropdownDTOs;
    }

    @Override
    public List<EphemeralEnvironmentResponse> getEphemeralEnvironmentList() {

        String userId = UserContextUtil.getUserIdFromRequestContext();
        if (Objects.isNull(userId)) {
            log.error("user id is not present");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        Optional<User> user = userRepository.findByUuid(userId);
        if (user.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Optional<List<EphemeralEnvironment>> ephemeralEnvironmentsOpt = environmentRepository.findByOwner(user.get());
        if (ephemeralEnvironmentsOpt.isEmpty() || ephemeralEnvironmentsOpt.get().isEmpty()) {
            log.error("No ephemeral environments found for user {}", userId);
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_LIST_NOT_FOUND);
        }

        List<EphemeralEnvironmentResponse> responseList = new ArrayList<>();
        for (EphemeralEnvironment env : ephemeralEnvironmentsOpt.get()) {
            responseList.add(mapEntityToResponse(env));
        }
        return responseList;
    }

    private EphemeralEnvironmentResponse mapEntityToResponse(EphemeralEnvironment environment) {

        EphemeralEnvironmentResponse environmentResponse = new EphemeralEnvironmentResponse();

        environmentResponse.setId(environment.getId());
        environmentResponse.setName(environment.getName());
        environmentResponse.setStatus(environment.getStatus());
        environmentResponse.setOwner(environment.getOwner().getEmailId());
        environmentResponse.setCreatedTime(environment.getCreatedTime());
        environmentResponse.setExpiryOn(environment.getExpiryOn());

        return environmentResponse;
    }

}
