package com.swadeshitech.prodhub.services;

import java.util.List;

import com.swadeshitech.prodhub.dto.DropdownDTO;

public interface ConstantsService {

    public List<DropdownDTO> getConstants(String name);
}
