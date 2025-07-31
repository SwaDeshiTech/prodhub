package com.swadeshitech.prodhub.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "organizations")
public class Organization extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    private boolean isActive;
}
