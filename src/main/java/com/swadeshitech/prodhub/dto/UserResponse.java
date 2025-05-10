package com.swadeshitech.prodhub.dto;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {

    private String uuid;
    private String name;
    private String userName;
    private String emailId;
    private String phoneNumber;
    private Boolean isActive;
    private String profilePicture;

    private Set<String> teams;
    private Set<String> departments;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date lastModifiedTime;

    private String createdBy;
    private String lastModifiedBy;
}
