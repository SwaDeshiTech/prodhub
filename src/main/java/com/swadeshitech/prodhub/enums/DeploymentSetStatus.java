package com.swadeshitech.prodhub.enums;

import lombok.Getter;

@Getter
public enum DeploymentSetStatus {
    CREATED("Created"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    FAILED("Failed");

    private final String message;

    DeploymentSetStatus(String message) {
        this.message = message;
    }
}
