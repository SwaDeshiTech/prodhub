package com.swadeshitech.prodhub.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class TeamResponse {

    private String name;
    private String description;
    private boolean isActive;
    private List<DepartmentResponse> departments;
    private List<UserResponse> employees;

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss")
    private LocalDateTime updateTime;

    private String createdBy;
    private String modifiedBy;
}
