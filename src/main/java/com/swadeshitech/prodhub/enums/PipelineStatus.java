package com.swadeshitech.prodhub.enums;

public enum PipelineStatus {
    /** Pipeline has been created but not yet picked up by the executor */
    PENDING("Pending"),

    /** Pipeline is currently executing stages */
    IN_PROGRESS("In Progress"),

    /** Pipeline is paused, waiting for a user to manually approve a stage (e.g., Production Deploy) */
    AWAITING_APPROVAL("Awaiting Approval"),

    /** Pipeline completed successfully across all mandatory stages */
    SUCCESS("Success"),

    /** At least one mandatory stage failed, halting the pipeline */
    FAILED("Failed"),

    /** The pipeline was manually stopped by a user while running */
    CANCELLED("Cancelled"),

    /** A stage exceeded its defined timeoutSeconds */
    TIMED_OUT("Timed Out"),

    /** The pipeline encountered a system-level error (e.g., Lost connection to K8s) */
    ERROR("Error"),

    /** A manual or automated action to revert the changes made by this pipeline */
    ROLLING_BACK("Rolling Back"),

    /** The pipeline has been successfully rolled back after a failure */
    ROLLED_BACK("Rolled Back");

    private final String message;

    PipelineStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
