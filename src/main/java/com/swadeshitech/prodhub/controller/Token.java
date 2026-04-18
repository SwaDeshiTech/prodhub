package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.TokenGenerateRequest;
import com.swadeshitech.prodhub.dto.TokenResponse;
import com.swadeshitech.prodhub.services.TokenService;
import com.swadeshitech.prodhub.utils.UserContextUtil;

@RestController
@RequestMapping("/api/v1/token")
public class Token {

    @Autowired
    private TokenService tokenService;

    @PostMapping("/generate")
    public ResponseEntity<Response> generateToken(
            @RequestBody TokenGenerateRequest request,
            @RequestHeader(name = "uuid") String uuid) {

        String userId = UserContextUtil.getUserId();
        String organizationId = UserContextUtil.getOrganizationId();

        TokenResponse tokenResponse = tokenService.generateToken(request, userId, organizationId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Token generated successfully")
                .response(tokenResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<Response> listTokens(@RequestHeader(name = "uuid") String uuid) {

        String userId = UserContextUtil.getUserId();
        String organizationId = UserContextUtil.getOrganizationId();

        List<TokenResponse> tokens = tokenService.listTokens(userId, organizationId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Tokens fetched successfully")
                .response(tokens)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Response> revokeToken(
            @PathVariable String tokenId,
            @RequestHeader(name = "uuid") String uuid) {

        String userId = UserContextUtil.getUserId();
        String organizationId = UserContextUtil.getOrganizationId();

        tokenService.revokeToken(tokenId, userId, organizationId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Token revoked successfully")
                .build();

        return ResponseEntity.ok().body(response);
    }
}
