package com.swadeshitech.prodhub.services;

import java.util.List;

import org.springframework.stereotype.Component;

import com.swadeshitech.prodhub.dto.BuildProviderResponse;
import com.swadeshitech.prodhub.dto.BuilderProviderRequest;

@Component
public interface BuildProviderService {

    BuildProviderResponse onboardBuildProvider(BuilderProviderRequest request);

    BuildProviderResponse getBuildProvider(String providerId);

    BuildProviderResponse updateBuildProvider(String providerId, BuilderProviderRequest request);

    List<BuildProviderResponse> getAllBuildProviders();

    List<BuildProviderResponse> registeredBuildProviders();

    String removeBuildProvider(String providerId);
}
