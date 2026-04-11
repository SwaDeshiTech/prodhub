package com.swadeshitech.prodhub.enums;

public enum PipelineStepExecutionStatus {
    /** Pipeline Step has been created but not yet picked up by the executor */
    PENDING("Pending"),

    /** Pipeline Step is currently executing stages */
    IN_PROGRESS("In Progress"),

    /** Pipeline Step is paused, waiting for a user to manually approve a stage (e.g., Production Deploy) */
    AWAITING_APPROVAL("Awaiting Approval"),

    /** Pipeline Step completed successfully across all mandatory stages */
    SUCCESS("Success"),

    /** At least one mandatory stage failed, halting the pipeline */
    FAILED("Failed"),

    /** The Pipeline Step was manually stopped by a user while running */
    CANCELLED("Cancelled"),

    /** A stage exceeded its defined timeoutSeconds */
    TIMED_OUT("Timed Out"),

    /** The Pipeline Step encountered a system-level error (e.g., Lost connection to K8s) */
    ERROR("Error"),

    /** A manual or automated action to revert the changes made by this pipeline */
    ROLLING_BACK("Rolling Back"),

    /** The Pipeline Step has been successfully rolled back after a failure */
    ROLLED_BACK("Rolled Back"),

    /** The Pipeline Step was skipped due to a previous stage failure */
    SKIPPED("Skipped");

    private final String message;

    PipelineStepExecutionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
