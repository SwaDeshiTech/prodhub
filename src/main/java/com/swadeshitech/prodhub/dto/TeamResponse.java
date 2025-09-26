package com.swadeshitech.prodhub.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class TeamResponse extends BaseResponse {

    private String name;
    private String description;
    private boolean isActive;
    private List<DepartmentResponse> departments;
    private List<UserResponse> employees;
    private List<UserResponse> managers;
    private List<ApplicationResponse> applications;
}
