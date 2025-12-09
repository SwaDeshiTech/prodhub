package com.swadeshitech.prodhub.enums;

import com.swadeshitech.prodhub.exception.CustomException;
import lombok.Getter;

@Getter
public enum CredentialProvider {
    GITHUB_ACTIONS("GitHub Actions", "buildProvider"),
    GITLAB_CI("GitLab CI", "buildProvider"),
    BITBUCKET_PIPELINES("Bitbucket Pipelines", "buildProvider"),
    CIRCLECI("CircleCI", "buildProvider"),
    TRAVIS_CI("Travis CI", "buildProvider"),
    JENKINS("Jenkins", "buildProvider"),
    AWS_CODEBUILD("AWS CodeBuild", "buildProvider"),
    GOOGLE_CLOUD_BUILD("Google Cloud Build", "buildProvider"),
    DOCKER_CONTAINER_REGISTRY("Docker Container Registry", "repositoryProvider"),
    JFROG_ARTIFACTORY("Jfrog Artifactory", "repositoryProvider"),
    BLOB_STORAGE("Blob Storage", "repositoryProvider"),
    NEXUS_REPOSITORY_MANAGER("Nexus Repository Manager", "repositoryProvider"),
    GIT_REPOSITORY("Git Repository", "repositoryProvider"),
    CUSTOM("Custom", ""),
    GITHUB("Github", "scm"),
    AWS("AWS", "cloudProvider"),
    GCP("GCP", "cloudProvider"),
    AZURE("Azure", "cloudProvider"),
    K8S("k8s", "deploymentProvider");

    private final String displayName;
    private final String type;

    CredentialProvider(String displayName, String type) {
        this.displayName = displayName;
        this.type = type;
    }

    public static CredentialProvider fromValue(String value) {
        for (CredentialProvider provider : CredentialProvider.values()) {
            if (provider.name().equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No CredentialProvider found for : " + value);
    }

    public static CredentialProvider fromType(String type) {
        for(CredentialProvider provider : CredentialProvider.values()) {
            if (provider.getType().equalsIgnoreCase(type)) {
                return provider;
            }
        }
        throw new CustomException(ErrorCode.CREDENTIAL_PROVIDER_NOT_FOUND);
    }
}
