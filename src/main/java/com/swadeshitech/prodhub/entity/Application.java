package com.swadeshitech.prodhub.entity;

import java.util.Set;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class Application extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    private boolean isActive;

    @DBRef
    private Team team;

    @DBRef
    private Department department;

    @DBRef
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Metadata> profiles;
}
