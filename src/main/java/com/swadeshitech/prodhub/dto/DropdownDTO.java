package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DropdownDTO {
    private String key; // id/uuid
    private String value; // display value
}