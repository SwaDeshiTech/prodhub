package com.swadeshitech.prodhub.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.swadeshitech.prodhub.dto.ProviderConstantResponse;
import com.swadeshitech.prodhub.enums.RunTimeEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swadeshitech.prodhub.dto.DropdownDTO;
import com.swadeshitech.prodhub.entity.Constants;
import com.swadeshitech.prodhub.enums.ErrorCode;
import com.swadeshitech.prodhub.exception.CustomException;
import com.swadeshitech.prodhub.repository.ConstantsRepository;
import com.swadeshitech.prodhub.services.ConstantsService;

@Service
public class ConstantsServiceImpl implements ConstantsService {

    @Autowired
    private ConstantsRepository constantsRepository;

    @Override
    public List<DropdownDTO> getConstants(String name) {

        Optional<Constants> optionalConstant = constantsRepository.findByName(name);
        if (optionalConstant.isEmpty()) {
            throw new CustomException(ErrorCode.CONSTANTS_NOT_FOUND);
        }

        List<DropdownDTO> dropdownDTOs = new ArrayList<>();

        for (String constant : optionalConstant.get().getValues()) {
            dropdownDTOs.add(DropdownDTO.builder().key(constant).value(constant).build());
        }

        return dropdownDTOs;
    }

    @Override
    public List<ProviderConstantResponse> getProviders(String name) {

        Optional<Constants> optionalConstant = constantsRepository.findByName(name);
        if (optionalConstant.isEmpty()) {
            throw new CustomException(ErrorCode.CONSTANTS_NOT_FOUND);
        }

        List<ProviderConstantResponse> providerConstantResponses = new ArrayList<>();
        for (String provider : optionalConstant.get().getValues()) {
            providerConstantResponses.add(ProviderConstantResponse.builder()
                    .name(provider)
                    .id(provider)
                    .location("/dashboard/connect/onboarding/" + name + "/" + provider)
                    .isActive(true).build());
        }
        return providerConstantResponses;
    }

    @Override
    public List<DropdownDTO> getRuntimeEnvironment() {

        List<DropdownDTO> dropdownDTOs = new ArrayList<>();

        for(RunTimeEnvironment runTimeEnvironment : RunTimeEnvironment.values()) {
            dropdownDTOs.add(DropdownDTO.builder()
                    .key(runTimeEnvironment.toString())
                    .value(runTimeEnvironment.toString())
                    .build());
        }

        return dropdownDTOs;
    }

}
