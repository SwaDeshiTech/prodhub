package com.swadeshitech.prodhub.enums;

public enum DeploymentStatus {
    CREATED("Created"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    FAILED("Failed");

    private final String message;

    DeploymentStatus(String message) {
        this.message = message;
    }
}
