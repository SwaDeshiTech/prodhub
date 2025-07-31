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
    EPHEMERAL_ENVIRONMENT_LIST_NOT_FOUND("PH-5003", "Ephemeral Environment list not found", HttpStatus.NOT_FOUND),
    EPHEMERAL_ENVIRONMENT_NOT_FOUND("PH-5004", "Ephemeral Environment could not be found", HttpStatus.NOT_FOUND),

    METADATA_PROFILE_ALREADY_EXISTS("PH-6001", "Meta data profile already exists", HttpStatus.BAD_REQUEST),
    METADATA_PROFILE_NOT_FOUND("PH-6002", "Meta data profile could not found", HttpStatus.BAD_REQUEST),

    CONSTANTS_NOT_FOUND("PH-7001", "Constants could not found", HttpStatus.NOT_FOUND),

    CLOUD_PROVIDERS_NOT_FOUND("PH-8001", "Cloud providers list could not found", HttpStatus.NOT_FOUND),
    CLOUD_PROVIDER_NOT_FOUND("PH-8002", "Cloud provider could not found", HttpStatus.NOT_FOUND),
    CLOUD_PROVIDER_COULD_NOT_DELETED("PH-8003", "Cloud provider could not be deleted", HttpStatus.BAD_REQUEST),

    ROLE_NOT_FOUND("PH-9001", "Role could not be found", HttpStatus.NOT_FOUND),

    TAB_NOT_FOUND("PH-10001", "Tabs could not be found", HttpStatus.NOT_FOUND),

    SCM_NOT_FOUND("PH-12001", "SCM could not be found", HttpStatus.NOT_FOUND),
    SCM_COULD_NOT_BE_REGISTERED("PH-12002", "SCM could not be registered", HttpStatus.BAD_REQUEST),
    SCM_COULD_NOT_BE_UPDATED("PH-12003", "SCM could not be updated", HttpStatus.BAD_REQUEST),
    SCM_COULD_NOT_BE_DELETED("PH-12004", "SCM could not be deleted", HttpStatus.BAD_REQUEST),

    ORGANIZATION_NOT_FOUND("PH-13001", "Organization could not be found", HttpStatus.NOT_FOUND),
    ORGANIZATION_ALREADY_EXISTS("PH-13002", "Organization already exists", HttpStatus.BAD_REQUEST),
    ORGANIZATION_COULD_NOT_BE_REGISTERED("PH-13003", "Organization could not be registered", HttpStatus.BAD_REQUEST),
    ORGANIZATION_COULD_NOT_BE_UPDATED("PH-13004", "Organization could not be updated", HttpStatus.BAD_REQUEST),
    ORGANIZATION_COULD_NOT_BE_DELETED("PH-13005", "Organization could not be deleted", HttpStatus.BAD_REQUEST);

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
