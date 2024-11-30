package com.swadeshitech.prodhub.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dob;

    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss")
    private LocalDateTime createTime;

    private String createdBy;
    private String modifiedBy;
}
