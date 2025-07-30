package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SCMRegisterResponse extends BaseResponse {
    private String id;
    private String name;
    private boolean isActive;
}
