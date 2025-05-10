package com.swadeshitech.prodhub.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest implements Serializable {
    private String phoneNumber;
    // Add more fields here as needed for future updates
}