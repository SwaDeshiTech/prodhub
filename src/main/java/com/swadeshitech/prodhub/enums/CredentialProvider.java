package com.swadeshitech.prodhub.enums;

public enum CredentialProvider {
    GITHUB_ACTIONS("GitHub Actions"),
    GITLAB_CI("GitLab CI"),
    BITBUCKET_PIPELINES("Bitbucket Pipelines"),
    CIRCLECI("CircleCI"),
    TRAVIS_CI("Travis CI"),
    JENKINS("Jenkins"),
    TEAMCITY("TeamCity"),
    AZURE_DEVOPS("Azure DevOps"),
    AWS_CODEBUILD("AWS CodeBuild"),
    GOOGLE_CLOUD_BUILD("Google Cloud Build"),
    DOCKER_HUB("Docker Hub"),
    CUSTOM("Custom");

    private final String displayName;

    CredentialProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CredentialProvider fromDisplayName(String displayName) {
        for (CredentialProvider provider : CredentialProvider.values()) {
            if (provider.getDisplayName().equalsIgnoreCase(displayName)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No CredentialProvider found for display name: " + displayName);
    }
}
