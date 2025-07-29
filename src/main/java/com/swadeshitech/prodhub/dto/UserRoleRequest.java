package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Data;

@Data
public class UserRoleRequest {
    Set<String> roles;
}
