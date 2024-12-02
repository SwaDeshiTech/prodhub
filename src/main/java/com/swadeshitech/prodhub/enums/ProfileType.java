package com.swadeshitech.prodhub.enums;

public enum ProfileType {
    
    BUILD("Build"),
    DEPLOYMENT("Deployment"),
    LOGGING("Logging"),
    DATASTORE("Data Store"),
    QUEUE("Queue");

    private String value;

    ProfileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
