package com.swadeshitech.prodhub.dto;

import com.swadeshitech.prodhub.entity.Metadata;
import com.swadeshitech.prodhub.enums.ProfileType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class MetaDataResponse extends BaseResponse {
    private String name;
    private String data;
    private ProfileType profileType;
    private boolean isActive;
    private String referencedProfileId;
    private String description;

    public static MetaDataResponse buildResponseObject(Metadata metadata) {
        return MetaDataResponse.builder()
                .id(metadata.getId())
                .name(metadata.getName())
                .isActive(metadata.isActive())
                .data(metadata.getData())
                .profileType(metadata.getProfileType())
                .referencedProfileId(
                        metadata.getReferencedProfile() != null ? metadata.getReferencedProfile().getId() : null)
                .description(metadata.getDescription())
                .createdBy(metadata.getCreatedBy())
                .createdTime(metadata.getCreatedTime())
                .lastModifiedBy(metadata.getLastModifiedBy())
                .lastModifiedTime(metadata.getLastModifiedTime())
                .build();
    }
}
