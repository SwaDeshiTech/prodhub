package com.swadeshitech.prodhub.service;

import com.swadeshitech.prodhub.dto.Response;
import java.util.Map;

public interface AuthService {
    Response internalLogin(Map<String, String> credentials);
    Response internalSignup(Map<String, String> userData);
}
