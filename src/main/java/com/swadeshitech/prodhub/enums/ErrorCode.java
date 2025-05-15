package com.swadeshitech.prodhub.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    MAPPING_ERROR("PH-0000", "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_INTEGRITY_FAILURE("PH-0001", "One or more mandatory fields are missing", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("PH-0002", "One or more mandatory fields are missing", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("PH-0003", "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_UPDATE_FAILED("PH-1001", "User could not be updated", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("PH-1002", "User not found", HttpStatus.NOT_FOUND),
    USER_UUID_NOT_FOUND("PH-1003", "User uuid not found", HttpStatus.BAD_REQUEST),
    USER_DETAILS_MISSING("PH-1004", "User details are missing", HttpStatus.BAD_REQUEST),

    DEPARTMENT_UPDATE_FAILED("PH-2001", "Department could not be updated", HttpStatus.INTERNAL_SERVER_ERROR),
    DEPARTMENT_NOT_FOUND("PH-2002", "Department not found", HttpStatus.NOT_FOUND),

    TEAM_UPDATE_FAILED("PH-3001", "Team could not be updated", HttpStatus.INTERNAL_SERVER_ERROR),
    TEAM_NOT_FOUND("PH-3002", "Team not found", HttpStatus.NOT_FOUND),
    TEAM_UUID_NOT_FOUND("PH-3003", "Team uuid not found", HttpStatus.BAD_REQUEST),

    APPLICATION_UPDATE_FAILED("PH-4001", "Application could not be updated", HttpStatus.INTERNAL_SERVER_ERROR),
    APPLICATION_NOT_FOUND("PH-4002", "Application not found", HttpStatus.NOT_FOUND),
    APPLICATION_LIST_NOT_FOUND("PH-4003", "Application list not found", HttpStatus.BAD_REQUEST),

    EPHEMERAL_ENVIRONMENT_ID_NOT_FOUND("PH-5001", "Ephemeral Environment could not be found", HttpStatus.NOT_FOUND),
    EPHEMERAL_ENVIRONMENT_UPDATE_FAILED("PH-5002", "Ephemeral Environment could not be updated",
            HttpStatus.INTERNAL_SERVER_ERROR),
    EPHEMERAL_ENVIRONMENT_LIST_NOT_FOUND("PH-5003", "Ephemeral Environment list not found", HttpStatus.NOT_FOUND);

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
