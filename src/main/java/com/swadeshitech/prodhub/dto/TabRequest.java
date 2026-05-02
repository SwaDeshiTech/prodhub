package com.swadeshitech.prodhub.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
// @Builder
public class TabRequest {

    private String id;
    private String name;
    private String link;
    private Integer sortOrder;
    private Set<String> roles;
    private String parentId;
    private Set<TabRequest> children;
}
