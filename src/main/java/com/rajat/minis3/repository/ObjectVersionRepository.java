package com.rajat.minis3.repository;

import com.rajat.minis3.model.ObjectVersion;
import com.rajat.minis3.model.StorageObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectVersionRepository extends JpaRepository<ObjectVersion, Long> {

    /**
     * Find all versions of a storage object ordered by version number descending
     * @param storageObject the storage object
     * @return list of object versions
     */
    List<ObjectVersion> findByStorageObjectOrderByVersionNumberDesc(StorageObject storageObject);

    /**
     * Find a specific version of a storage object
     * @param storageObject the storage object
     * @param versionNumber the version number
     * @return Optional containing the object version if found
     */
    Optional<ObjectVersion> findByStorageObjectAndVersionNumber(StorageObject storageObject, Integer versionNumber);

    /**
     * Find the latest version of a storage object
     * @param storageObject the storage object
     * @param isLatest true to get the latest version
     * @return Optional containing the latest object version
     */
    Optional<ObjectVersion> findByStorageObjectAndIsLatest(StorageObject storageObject, Boolean isLatest);

    /**
     * Count the number of versions for a storage object
     * @param storageObject the storage object
     * @return number of versions
     */
    long countByStorageObject(StorageObject storageObject);
}
