package com.swadeshitech.prodhub.enums;

import lombok.Getter;

@Getter
public enum RunTimeEnvironment {

    K8s("k8s", "deploymentK8s"),
    VM("vm", "deploymentvm");

    private final String runTimeEnvironment;
    private final String deploymentTemplate;

    RunTimeEnvironment(String runTimeEnvironment, String deploymentTemplate) {
        this.runTimeEnvironment = runTimeEnvironment;
        this.deploymentTemplate = deploymentTemplate;
    }

    public RunTimeEnvironment fromName(String value) {
        for (RunTimeEnvironment environment : RunTimeEnvironment.values()) {
            if(environment.name().equals(value)) {
                return environment;
            }
        }
        return null;
    }

}
