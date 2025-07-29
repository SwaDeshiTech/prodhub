package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
// @Builder
public class TabRequest {

    private String name;
    private String link;
    private Set<String> roles;
    private Set<TabRequest> children;
}
