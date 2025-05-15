package com.swadeshitech.prodhub.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * ApplicationRequest is a DTO class that represents the request body for
 * creating or updating an application.
 * It contains fields such as name, description, teamId, departmentId, and
 * profiles.
 * The class is annotated with @Data to generate getters, setters, equals,
 * hashCode, and toString methods.
 * It also uses @JsonIgnoreProperties to ignore any unknown properties during
 * JSON deserialization.
 */
public class ApplicationRequest {

    private String name;
    private String description;
    private String teamId;
    private String departmentId;
    private Set<MetaDataRequest> profiles;
}
