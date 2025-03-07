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
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "teams")
public class Team extends BaseEntity implements Serializable {
    
    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    private boolean isActive;

    @DBRef
    private Set<User> employees;

    @DBRef
    private Set<Department> departments;
}
