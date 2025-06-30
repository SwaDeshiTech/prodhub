package com.swadeshitech.prodhub.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "resourceDetails")
public class ResourceDetails extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed
    private String name;

    private String resourceType;

    @Indexed
    private String cloudProvider;

    private String meta;
}
