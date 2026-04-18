package com.swadeshitech.prodhub.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.dto.TokenGenerateRequest;
import com.swadeshitech.prodhub.dto.TokenResponse;
import com.swadeshitech.prodhub.entity.Token;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.TokenRepository;
import com.swadeshitech.prodhub.services.TokenService;
import com.swadeshitech.prodhub.utils.Base64Util;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public TokenResponse generateToken(TokenGenerateRequest request, String userId, String organizationId) {
        log.info("Generating token for user: {}, organization: {}", userId, organizationId);

        // Check token limit - users can have max 5 active tokens
        List<Token> existingTokens = tokenRepository.findByUserIdAndActiveTrue(userId);
        if (existingTokens.size() >= 5) {
            log.warn("Token limit exceeded for user: {}. Current count: {}", userId, existingTokens.size());
            throw new CustomException(ErrorCode.TOKEN_LIMIT_EXCEEDED);
        }

        // Generate a unique token ID
        String tokenId = UUID.randomUUID().toString();

        // Generate the actual token (using a simple method for now - in production, use a more secure method)
        String token = generateSecureToken();

        // Calculate expiry date
        LocalDateTime expiresAt = null;
        if (request.getExpiryDays() != null && request.getExpiryDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpiryDays());
        }

        // Create the token entity
        Token tokenEntity = Token.builder()
                .tokenId(tokenId)
                .tokenHash(hashToken(token))
                .description(request.getDescription())
                .expiryDays(request.getExpiryDays())
                .expiresAt(expiresAt)
                .userId(userId)
                .organizationId(organizationId)
                .active(true)
                .build();

        // Save to database
        tokenRepository.save(tokenEntity);

        log.info("Token generated successfully with tokenId: {}", tokenId);

        // Return the response with the actual token (only shown once)
        return TokenResponse.builder()
                .tokenId(tokenId)
                .token(token)
                .description(request.getDescription())
                .expiryDays(request.getExpiryDays())
                .expiresAt(expiresAt)
                .active(true)
                .createdTime(tokenEntity.getCreatedTime())
                .build();
    }

    @Override
    public List<TokenResponse> listTokens(String userId, String organizationId) {
        log.info("Listing tokens for user: {}, organization: {}", userId, organizationId);

        List<Token> tokens = tokenRepository.findByUserIdAndActiveTrue(userId);

        return tokens.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void revokeToken(String tokenId, String userId, String organizationId) {
        log.info("Revoking token: {} for user: {}, organization: {}", tokenId, userId, organizationId);

        Token token = tokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND));

        // Verify the token belongs to the user
        if (!token.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Deactivate the token
        token.setActive(false);

        tokenRepository.save(token);

        log.info("Token revoked successfully: {}", tokenId);
    }

    @Override
    public TokenResponse getTokenById(String tokenId, String userId, String organizationId) {
        log.info("Fetching token: {} for user: {}, organization: {}", tokenId, userId, organizationId);

        Token token = tokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND));

        // Verify the token belongs to the user
        if (!token.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return mapToResponse(token);
    }

    private String generateSecureToken() {
        // Generate a random token (in production, use a more secure method like JWT)
        return "prodhub_" + UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis();
    }

    private String hashToken(String token) {
        // Hash the token for storage (in production, use a proper hashing algorithm like bcrypt)
        return Base64Util.convertToPlainText(token);
    }

    private TokenResponse mapToResponse(Token token) {
        return TokenResponse.builder()
                .tokenId(token.getTokenId())
                .description(token.getDescription())
                .expiryDays(token.getExpiryDays())
                .expiresAt(token.getExpiresAt())
                .active(token.isActive())
                .createdTime(token.getCreatedTime())
                .lastUsedAt(token.getLastUsedAt())
                .token(null) // Never include token in list responses
                .build();
    }
}
