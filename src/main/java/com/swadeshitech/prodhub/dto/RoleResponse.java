package com.swadeshitech.prodhub.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RoleResponse extends BaseResponse {
    private String name;
    private boolean isDefault;
}
