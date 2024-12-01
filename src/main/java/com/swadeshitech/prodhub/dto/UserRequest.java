package com.swadeshitech.prodhub.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest implements Serializable {
    private String uuid;
    private String name;
    private String userName;
    private String emailId;
    private String phoneNumber;
    private LocalDate dob;
}
