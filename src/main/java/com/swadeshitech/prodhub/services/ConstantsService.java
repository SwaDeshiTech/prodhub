package com.swadeshitech.prodhub.services;

import java.util.List;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.ProviderConstantResponse;

public interface ConstantsService {

    List<DropdownDTO> getConstants(String name);

    List<ProviderConstantResponse> getProviders(String name);

    List<DropdownDTO> getRuntimeEnvironment();

    List<DropdownDTO> getK8sClusterDropdown();
}
