package com.rajat.minis3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rajat.minis3.model.Bucket;
import com.rajat.minis3.model.Object;

@Repository
public interface ObjectRepository extends JpaRepository<Object, String> {

    /**
     * Find a storage object by bucket and object key
     * @param bucket the bucket
     * @param objectKey the object key
     * @return Optional containing the storage object if found
     */
    Optional<Object> findByBucketAndObjectKey(Bucket bucket, String objectKey);

    /**
     * Find all storage objects in a bucket
     * @param bucket the bucket
     * @return list of storage objects
     */
    List<Object> findByBucket(Bucket bucket);

    /**
     * Check if a storage object exists with the given bucket and object key
     * @param bucket the bucket
     * @param objectKey the object key
     * @return true if exists, false otherwise
     */
    boolean existsByBucketAndObjectKey(Bucket bucket, String objectKey);

    String findObjectContentByBucketAndObjectKey(Bucket bucket, String objectKey);
}
