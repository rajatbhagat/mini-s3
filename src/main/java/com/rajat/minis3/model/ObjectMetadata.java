package com.rajat.minis3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "object_metadata",
       uniqueConstraints = @UniqueConstraint(columnNames = {"version_id", "meta_key"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ObjectVersion objectVersion;

    @Column(name = "meta_key", nullable = false, length = 255)
    private String key;

    @Column(name = "meta_value", columnDefinition = "TEXT")
    private String value;
}
