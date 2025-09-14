package com.swadeshitech.prodhub.enums;

public enum ProfileType {

    BUILD("build", "Build pipeline settings"),
    DEPLOYMENT("deployment", "Deployment configuration"),
    LOGGING("logging", "Logging & observability"),
    DATASTORE("dataStore", "Datastore configuration"),
    QUEUE("queue", "Queue/broker configuration"),
    APPROVAL("approval", "Approval group configuration"); // <-- message for approval group

    private final String value;
    private final String message;

    ProfileType(String value, String message) {
        this.value = value;
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    public static ProfileType fromValue(String value) {
        for (ProfileType pt : values()) {
            if (pt.value.equalsIgnoreCase(value)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("Unknown ProfileType value: " + value);
    }
}