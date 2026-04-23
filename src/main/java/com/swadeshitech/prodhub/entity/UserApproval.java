package com.swadeshitech.prodhub.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_approvals")
@EqualsAndHashCode(callSuper = true)
@Builder
public class UserApproval extends BaseEntity implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Reference to User.id

    private String userName;

    private String userEmail;

    private boolean approved; // true if user is approved to use the app

    private boolean blocked; // true if user is explicitly blocked

    private String approvedBy; // User ID of admin who approved

    private String blockedBy; // User ID of admin who blocked

    private String rejectionReason; // Reason for blocking/rejection
}
