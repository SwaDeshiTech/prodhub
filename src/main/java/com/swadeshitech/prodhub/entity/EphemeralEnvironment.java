package com.swadeshitech.prodhub.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.swadeshitech.prodhub.enums.EphemeralEnvrionmentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ephemeralEnvironments")
public class EphemeralEnvironment extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private LocalDateTime expiryOn;

    private EphemeralEnvrionmentStatus status;

    private List<Profile> attachedProfiles;

    @DBRef
    private User owner;

    @DBRef
    private Set<User> sharedWith;

    @Data
    @Builder
    public static class Profile {

        @DBRef
        private Metadata buildProfile;

        @DBRef
        private Metadata deploymentProfile;

        @DBRef
        private Application application;
    }
}