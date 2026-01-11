package com.rajat.minis3.dao;

import com.rajat.minis3.config.OperationResult;
import com.rajat.minis3.model.Bucket;

import java.util.Optional;

public interface BucketDao {

    Optional<Bucket> getBucketDetails(String bucketName);
    OperationResult dropBucket(String bucketName);
    Optional<Bucket> createBucketFromName(String bucketName);

}
