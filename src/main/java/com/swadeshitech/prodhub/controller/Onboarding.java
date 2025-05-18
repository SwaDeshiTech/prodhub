package com.swadeshitech.prodhub.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadeshitech.prodhub.dto.ApplicationProfileRequest;
import com.swadeshitech.prodhub.dto.ApplicationProfileResponse;
import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.MetaDataResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.OnboardingService;

@RestController
@RequestMapping("/onboarding")
public class Onboarding {

    @Autowired
    private OnboardingService onboardingService;

    @GetMapping("/{onboardingType}/{id}")
    public ResponseEntity<Response> getOnboardingType(@PathVariable("onboardingType") String onboardingType,
            @PathVariable("id") String id) {

        Set<DropdownDTO> dropdownDTOs = onboardingService.getProfilesForDropdown(onboardingType, id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message(onboardingType + " has been fetched successfully")
                .response(dropdownDTOs)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Response> getOnboardingTypeDetails(@PathVariable String id) {

        MetaDataResponse metaDataResponse = onboardingService.getProfileDetails(id);

        Response response = Response.builder()
                .httpStatus(HttpStatus.ACCEPTED)
                .message("Profile has been fetched successfully")
                .response(metaDataResponse)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Response> onboardProfile(@RequestBody ApplicationProfileRequest request) {

        ApplicationProfileResponse applicationProfileResponse = onboardingService.onboardProfile(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Profile has been onboarded successfully")
                .response(applicationProfileResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<Response> updateProfile(@RequestBody ApplicationProfileRequest request) {

        MetaDataResponse metaDataResponse = onboardingService.updateProfile(request);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Profile has been updated successfully")
                .response(metaDataResponse)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
