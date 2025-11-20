package com.swadeshitech.prodhub.enums;

public enum DeploymentRunStatus {
    CREATED("Created"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    FAILED("Failed");

    private final String message;

    DeploymentRunStatus(String message) {
        this.message = message;
    }
}
