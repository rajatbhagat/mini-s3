package com.rajat.minis3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "object_version",
       uniqueConstraints = @UniqueConstraint(columnNames = {"object_id", "version_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ObjectVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", nullable = false)
    private StorageObject storageObject;

    @Column(nullable = false)
    private Integer versionNumber;

    @Lob
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] content;

    @Column(length = 255)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(length = 64)
    private String etag;

    @Column(nullable = false)
    private Boolean isLatest = true;

    @OneToMany(mappedBy = "objectVersion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ObjectMetadata> metadata = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
