package com.swadeshitech.prodhub.dto;

import java.util.List;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class TabResponse extends BaseResponse {
    private String name;
    private String link;
    private List<TabResponse> children;
}
