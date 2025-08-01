package com.swadeshitech.prodhub.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "buildProviders")
@EqualsAndHashCode(callSuper = true)
@Builder
public class BuildProvider extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private boolean isActive;

    private String metaData;
}
