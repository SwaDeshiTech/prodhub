package com.swadeshitech.prodhub.entity;

import java.io.Serializable;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import com.swadeshitech.prodhub.enums.ProfileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Metadata extends BaseEntity implements Serializable {

    @Id
    private Long id;
    
    private String name;

    @BsonRepresentation(BsonType.STRING)
    private ProfileType profileType;

    private String data;

    private boolean isActive;

    @DBRef
    private Application application;
}
