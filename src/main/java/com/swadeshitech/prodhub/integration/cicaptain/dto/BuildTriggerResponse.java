package com.swadeshitech.prodhub.integration.cicaptain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BuildTriggerResponse (
        @JsonProperty("data") Data data,
        @JsonProperty("message") String message
) {
    public record Data(
            @JsonProperty("build_id") String buildId,
            @JsonProperty("queue_id") String queueId,
            @JsonProperty("status") String status,
            @JsonProperty("url") String url,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") String timestamp,
            @JsonProperty("metadata") Metadata metadata,
            @JsonProperty("jenkins_details") JenkinsDetails jenkinsDetails
    ) {}

    public record Metadata(
            @JsonProperty("jenkins_base_url") String jenkinsBaseUrl,
            @JsonProperty("jenkins_build_number") String jenkinsBuildNumber,
            @JsonProperty("jenkins_build_type") String jenkinsBuildType,
            @JsonProperty("jenkins_build_url") String jenkinsBuildUrl,
            @JsonProperty("jenkins_job_name") String jenkinsJobName,
            @JsonProperty("jenkins_job_parameters") String jenkinsJobParameters,
            @JsonProperty("jenkins_queue_id") String jenkinsQueueId,
            @JsonProperty("jenkins_queue_url") String jenkinsQueueUrl,
            @JsonProperty("jenkins_trigger_time") String jenkinsTriggerTime,
            @JsonProperty("jenkins_triggered_by") String jenkinsTriggeredBy
    ) {}

    public record JenkinsDetails(
            @JsonProperty("build_number") String buildNumber,
            @JsonProperty("queue_id") String queueId,
            @JsonProperty("queue_url") String queueUrl,
            @JsonProperty("job_name") String jobName,
            @JsonProperty("base_url") String baseUrl,
            @JsonProperty("build_url") String buildUrl,
            @JsonProperty("trigger_time") String triggerTime,
            @JsonProperty("build_type") String buildType,
            @JsonProperty("parameters") JenkinsParameters parameters,
            @JsonProperty("triggered_by") String triggeredBy
    ) {}

    public record JenkinsParameters(
            @JsonProperty("BASE_IMAGE") String baseImage,
            @JsonProperty("BRANCH_NAME") String branchName,
            @JsonProperty("BUILD_COMMAND") String buildCommand,
            @JsonProperty("REPO_URL") String repoUrl
    ) {}
}
