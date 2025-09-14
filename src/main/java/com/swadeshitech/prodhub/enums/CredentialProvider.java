package com.swadeshitech.prodhub.enums;

public enum CredentialProvider {
    GITHUB_ACTIONS("GitHub Actions", "buildProvider"),
    GITLAB_CI("GitLab CI", "buildProvider"),
    BITBUCKET_PIPELINES("Bitbucket Pipelines", "buildProvider"),
    CIRCLECI("CircleCI", "buildProvider"),
    TRAVIS_CI("Travis CI", "buildProvider"),
    JENKINS("Jenkins", "buildProvider"),
    AZURE_DEVOPS("Azure DevOps", ""),
    AWS_CODEBUILD("AWS CodeBuild", "buildProvider"),
    GOOGLE_CLOUD_BUILD("Google Cloud Build", "buildProvider"),
    DOCKER_HUB("Docker Hub", ""),
    CUSTOM("Custom", ""),
    GITHUB("Github", "scm"),
    AWS("AWS", "cloudProvider"),
    GCP("GCP", "cloudProvider"),
    AZURE("Azure", "cloudProvider");

    private final String displayName;
    private final String type;

    CredentialProvider(String displayName, String type) {
        this.displayName = displayName;
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() { return type; }

    public static CredentialProvider fromDisplayName(String displayName) {
        for (CredentialProvider provider : CredentialProvider.values()) {
            if (provider.getDisplayName().equalsIgnoreCase(displayName)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No CredentialProvider found for display name: " + displayName);
    }
}
