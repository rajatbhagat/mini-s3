package com.rajat.minis3.repository;

import com.rajat.minis3.model.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BucketRepository extends JpaRepository<Bucket, Long> {

    /**
     * Find a bucket by its name
     * @param name the bucket name
     * @return Optional containing the bucket if found
     */
    Optional<Bucket> findByName(String name);

    /**
     * Check if a bucket exists with the given name
     * @param name the bucket name
     * @return true if bucket exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Creates a new Bucket with the provided bucket name
     * @param bucket Bucket obejct
     */
    Bucket save(Bucket bucket);
}
