package com.swadeshitech.prodhub.services;

import java.util.List;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.dto.ProviderConstantResponse;

public interface ConstantsService {

    public List<DropdownDTO> getConstants(String name);

    public List<ProviderConstantResponse> getProviders(String name);
}
