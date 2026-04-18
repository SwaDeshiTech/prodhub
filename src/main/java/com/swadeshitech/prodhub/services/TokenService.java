package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.TokenGenerateRequest;
import com.swadeshitech.prodhub.dto.TokenResponse;
import com.swadeshitech.prodhub.entity.Token;

@Component
public interface TokenService {

    TokenResponse generateToken(TokenGenerateRequest request, String userId, String organizationId);

    List<TokenResponse> listTokens(String userId, String organizationId);

    void revokeToken(String tokenId, String userId, String organizationId);

    TokenResponse getTokenById(String tokenId, String userId, String organizationId);
}
