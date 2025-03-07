package com.swadeshitech.prodhub.dto;

import java.util.Date;
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
    private boolean isActive;

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss")
    private Date createdTime;

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss")
    private Date lastModifiedTime;

    private String createdBy;
    private String lastModifiedBy;
}
