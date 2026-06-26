package com.thirdeye30.interviewprep.entities;

import com.thirdeye30.interviewprep.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "explorer", 
    indexes = {
        @Index(name = "idx_explorer_parent_uuid", columnList = "parent_uuid"),
        @Index(name = "idx_explorer_name", columnList = "name")
    }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Explorer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "parent_uuid")
    private UUID parentUuid;

    @Column(nullable = false)
    private String name;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "download_count", nullable = false)
    private Long downloadCount;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
        this.viewCount = 0L;
        this.downloadCount = 0L;
    }
}