package com.swadeshitech.prodhub.services;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.UserRequest;
import com.swadeshitech.prodhub.dto.UserResponse;

@Component
public interface UserService {
    
    public UserResponse getUserDetail(String uuid);

    public UserResponse addUser(UserRequest userRequest);
}