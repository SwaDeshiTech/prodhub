package com.swadeshitech.prodhub.services.impl;

import java.util.Objects;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.swadeshitech.prodhub.dto.UserRequest;
import com.swadeshitech.prodhub.dto.UserResponse;
import com.swadeshitech.prodhub.entity.User;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.UserRepository;
import com.swadeshitech.prodhub.services.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public UserResponse getUserDetail(String uuid) {

        if (StringUtils.isEmpty(uuid)) {
            log.error("user uuid is empty/null");
            throw new CustomException(ErrorCode.USER_UUID_NOT_FOUND);
        }

        User user = userRepository.findByUuid(uuid);
        if (Objects.isNull(user)) {
            log.error("user not found");
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        return userResponse;
    }

    @Override
    public UserResponse addUser(UserRequest userRequest) {
        
        User user = modelMapper.map(userRequest, User.class);
        if (Objects.isNull(user)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        
        user.setIsActive(Boolean.TRUE);

        saveUserDetailsToRepository(user);
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        return userResponse;
    }

    private User saveUserDetailsToRepository(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrity error ", ex);
            throw new CustomException(ErrorCode.DATA_INTEGRITY_FAILURE);
        } catch (PersistenceException ex) {
            log.error("Failed to save data ", ex);
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

}
