package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
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
@Document(collection = "departments")
public class Department extends BaseEntity implements Serializable {

    @Id
    @Indexed(unique = true)
    private String name;
    
    private String description;

    private boolean isActive;

    @DBRef
    private Set<Team> teams;

    @DBRef
    private Set<User> users;
}
