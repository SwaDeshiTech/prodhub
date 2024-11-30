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
    private static final long serialVersionUID = 4408418647685225829L;
    private String uuid;
    private String name;
    private String userName;
    private String emailId;
    private String phoneNumber;
    private LocalDate dob;
}
