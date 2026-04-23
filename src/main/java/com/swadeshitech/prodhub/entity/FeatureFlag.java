package com.swadeshitech.prodhub.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "feature_flags")
@EqualsAndHashCode(callSuper = true)
@Builder
public class FeatureFlag extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String key;

    private String name;

    private String description;

    private boolean enabled;

    private String category; // AUTH, GENERAL, SECURITY, etc.

    private String defaultValue;

    private String dataType; // BOOLEAN, STRING, NUMBER, JSON
}
