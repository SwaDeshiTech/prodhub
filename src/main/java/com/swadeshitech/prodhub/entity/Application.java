package com.swadeshitech.prodhub.entity;

import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Set<Metadata> profiles;
}
