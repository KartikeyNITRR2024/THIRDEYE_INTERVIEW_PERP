package com.thirdeye30.interviewprep.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.thirdeye30.interviewprep.enums.FileType;
import com.thirdeye30.interviewprep.enums.AccessType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("FILE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File extends Explorer {

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "internal_document_id")
    private UUID internalDocumentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;

    @Column(name = "size_in_bytes")
    private Long sizeInBytes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type")
    private AccessType accessType;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(columnDefinition = "TEXT")
    private String description;
}