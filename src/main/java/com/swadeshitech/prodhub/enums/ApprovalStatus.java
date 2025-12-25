package com.swadeshitech.prodhub.enums;

public enum ApprovalStatus {
    CREATED("Created"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELED("Canceled"),
    REASSIGNED("Reassigned"),
    SKIPPED("Skipped");

    private final String displayName;

    ApprovalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ApprovalStatus fromDisplayName(String displayName) {
        for (ApprovalStatus status : ApprovalStatus.values()) {
            if (status.getDisplayName().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No ApprovalStatus found for display name: " + displayName);
    }
}
