package com.swadeshitech.prodhub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "tokens")
public class Token extends BaseEntity {
    
    @Id
    private ObjectId id;
    
    @Indexed
    private String tokenId;
    
    private String tokenHash;
    
    private String description;
    
    private Integer expiryDays;
    
    private LocalDateTime expiresAt;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String organizationId;
    
    private boolean active;
    
    private LocalDateTime lastUsedAt;
}
