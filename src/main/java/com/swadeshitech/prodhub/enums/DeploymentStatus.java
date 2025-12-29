package com.swadeshitech.prodhub.enums;

import lombok.Getter;

@Getter
public enum DeploymentStatus {
    CREATED("Created"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    SKIPPED("Skipped");

    private final String message;

    DeploymentStatus(String message) {
        this.message = message;
    }
}
