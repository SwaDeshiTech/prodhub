package com.swadeshitech.prodhub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.UserOrganizationRequest;
import com.swadeshitech.prodhub.dto.UserOrganizationResponse;
import com.swadeshitech.prodhub.services.UserOrganizationService;
import com.swadeshitech.prodhub.utils.UserContextUtil;

@RestController
@RequestMapping("/user-organization")
public class UserOrganization {

    @Autowired
    private UserOrganizationService userOrganizationService;

    @PostMapping
    public ResponseEntity<Response> addUserToOrganization(
            @RequestBody UserOrganizationRequest request,
            @RequestHeader(name = "uuid") String uuid) {

        UserOrganizationResponse response = userOrganizationService.addUserToOrganization(request);

        Response res = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("User added to organization successfully")
                .response(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getOrganizationsForUser(@PathVariable String userId) {

        List<UserOrganizationResponse> organizations = userOrganizationService.getOrganizationsForUser(userId);

        Response res = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Organizations fetched successfully")
                .response(organizations)
                .build();

        return ResponseEntity.ok(res);
    }

    @GetMapping("/user/organizations")
    public ResponseEntity<Response> getCurrentUserOrganizations(@RequestHeader(name = "uuid") String uuid) {

        String userId = UserContextUtil.getUserIdFromRequestContext();
        List<UserOrganizationResponse> organizations = userOrganizationService.getOrganizationsForUser(userId);

        Response res = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Organizations fetched successfully")
                .response(organizations)
                .build();

        return ResponseEntity.ok(res);
    }

    @PostMapping("/csv-upload")
    public ResponseEntity<Response> addUsersToOrganizationViaCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("organizationId") String organizationId,
            @RequestHeader(name = "uuid") String uuid) {

        List<UserOrganizationResponse> responses = userOrganizationService
                .addUsersToOrganizationViaCsv(file, organizationId);

        Response res = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Users added to organization successfully via CSV")
                .response(responses)
                .build();

        return ResponseEntity.ok(res);
    }

    @GetMapping("/can-create-organization")
    public ResponseEntity<Response> canUserCreateOrganization(@RequestHeader(name = "uuid") String uuid) {

        String userId = UserContextUtil.getUserIdFromRequestContext();
        boolean canCreate = userOrganizationService.canUserCreateOrganization(userId);

        Response res = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Organization creation permission checked")
                .response(canCreate)
                .build();

        return ResponseEntity.ok(res);
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<Response> getOrganizationMembers(@PathVariable String organizationId) {

        List<UserOrganizationResponse> members = userOrganizationService.getOrganizationMembers(organizationId);

        Response res = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Organization members fetched successfully")
                .response(members)
                .build();

        return ResponseEntity.ok(res);
    }
}
