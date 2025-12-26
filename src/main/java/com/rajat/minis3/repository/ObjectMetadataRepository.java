package com.rajat.minis3.repository;

import com.rajat.minis3.model.ObjectMetadata;
import com.rajat.minis3.model.ObjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectMetadataRepository extends JpaRepository<ObjectMetadata, Long> {

    /**
     * Find all metadata for an object version
     * @param objectVersion the object version
     * @return list of metadata entries
     */
    List<ObjectMetadata> findByObjectVersion(ObjectVersion objectVersion);

    /**
     * Find metadata by object version and key
     * @param objectVersion the object version
     * @param key the metadata key
     * @return Optional containing the metadata if found
     */
    Optional<ObjectMetadata> findByObjectVersionAndKey(ObjectVersion objectVersion, String key);

    /**
     * Delete all metadata for an object version
     * @param objectVersion the object version
     */
    void deleteByObjectVersion(ObjectVersion objectVersion);
}
