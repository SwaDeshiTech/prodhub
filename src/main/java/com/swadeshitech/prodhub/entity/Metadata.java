package com.swadeshitech.prodhub.entity;

import java.io.Serializable;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.swadeshitech.prodhub.enums.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "metadata")
public class Metadata extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed
    private String name;

    @BsonRepresentation(BsonType.STRING)
    private ProfileType profileType;

    private String data;

    private boolean isActive;

    private String description;

    @DBRef
    private transient Metadata referencedProfile;

    @DBRef
    private transient Application application;
}
