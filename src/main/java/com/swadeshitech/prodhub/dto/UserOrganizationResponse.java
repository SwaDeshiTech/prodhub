package com.swadeshitech.prodhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserOrganizationResponse extends BaseResponse {
    private String userId;
    private String organizationId;
    private String organizationName;
    private String role;
}
