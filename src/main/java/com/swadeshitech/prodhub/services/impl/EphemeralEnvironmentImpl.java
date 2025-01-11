package com.swadeshitech.prodhub.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.swadeshitech.prodhub.dto.EphemeralEnvironmentRequest;
import com.swadeshitech.prodhub.dto.EphemeralEnvironmentResponse;
import com.swadeshitech.prodhub.entity.Application;
import com.swadeshitech.prodhub.entity.EphemeralEnvironment;
import com.swadeshitech.prodhub.entity.Team;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ApplicationRepository;
import com.swadeshitech.prodhub.repository.EphemeralEnvironmentRepository;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.services.EphemeralEnvironmentService;

import jakarta.persistence.PersistenceException;
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

        if(Objects.isNull(environment)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        
        environment.setActive(true);

        setOwnerDetail(environment, "123-123-123");

        saveEphemeralEnvironmentToRepository(environment);

        EphemeralEnvironmentResponse ephemeralEnvironmentResponse = modelMapper.map(environment, EphemeralEnvironmentResponse.class);

        return ephemeralEnvironmentResponse;
    }

    @Override
    public EphemeralEnvironmentResponse getEphemeralEnvironmentDetail(String id) {
        
        if(!StringUtils.hasText(id)) {
            log.error("ephemeral environment id is not present");
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_ID_NOT_FOUND);
        }

        Optional<EphemeralEnvironment> environment = environmentRepository.findById(id);
        if(environment.isEmpty()) {
            log.error("ephemeral environment could not be found", id);
            throw new CustomException(ErrorCode.EPHEMERAL_ENVIRONMENT_ID_NOT_FOUND);
        }

        EphemeralEnvironmentResponse ephemeralEnvironmentResponse = modelMapper.map(environment.get(), EphemeralEnvironmentResponse.class);

        return ephemeralEnvironmentResponse;
    }

    private EphemeralEnvironment saveEphemeralEnvironmentToRepository(EphemeralEnvironment environment) {
        try {
            return environmentRepository.save(environment);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (PersistenceException ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    private void setOwnerDetail(EphemeralEnvironment environment, String uuId) {
        
        Optional<User> user = userRepository.findByUuid(uuId);
        if(user.isEmpty()) {
            log.error("user could not be found", uuId);
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        environment.setOwner(user.get());
    }

    private void setApplications(EphemeralEnvironment environment, Set<String> applications) {
        
        List<Application> applicationList = applicationRepository.findAllById(applications);

        
    }
    
}
