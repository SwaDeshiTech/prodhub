package com.swadeshitech.prodhub.dto;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private String message;
    private HttpStatus httpStatus;
    private Object response;
    private String errorCode;

}
