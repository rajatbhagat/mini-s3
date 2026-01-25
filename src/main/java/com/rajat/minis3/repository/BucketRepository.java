package com.rajat.minis3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rajat.minis3.model.Bucket;

@Repository
public interface BucketRepository extends JpaRepository<Bucket, String> {

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
}
