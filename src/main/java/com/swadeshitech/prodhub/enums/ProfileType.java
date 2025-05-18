package com.swadeshitech.prodhub.enums;

public enum ProfileType {

    BUILD("build"),
    DEPLOYMENT("deployment"),
    LOGGING("logging"),
    DATASTORE("dataStore"),
    QUEUE("queue");

    private String value;

    ProfileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
