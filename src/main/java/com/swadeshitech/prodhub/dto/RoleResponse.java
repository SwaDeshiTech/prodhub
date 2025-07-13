package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse extends BaseResponse {
    private String name;
    private boolean isDefault;
}
