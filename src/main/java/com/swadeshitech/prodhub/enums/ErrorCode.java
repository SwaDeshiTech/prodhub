package com.swadeshitech.prodhub.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    MAPPING_ERROR("PH-0000", "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_INTEGRITY_FAILURE("PH-0001", "One or more mandatory fields are missing", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("PH-0002", "One or more mandatory fields are missing", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("PH-0003", "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_UPDATE_FAILED("PH-1001", "User could not be updated", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("PH-1002", "User not found", HttpStatus.NOT_FOUND),
    USER_UUID_NOT_FOUND("PH-1003", "User uuid not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus status;


    ErrorCode(String code, String description, HttpStatus status) {
        this.code = code;
        this.description = description;
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}