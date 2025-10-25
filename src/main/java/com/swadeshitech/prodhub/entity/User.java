package com.swadeshitech.prodhub.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "users")
public class User extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String uuid;

    private String name;

    private String phoneNumber;

    @Indexed(unique = true)
    private String emailId;

    private Boolean isActive;

    private String profilePicture;

    @DBRef
    private Set<Team> teams;

    @DBRef
    private Set<Department> departments;

    @DBRef
    private Set<Role> roles;

    public String getNameAndEmailId() {
        return this.name + " (" + this.emailId + ")";
    }
}
