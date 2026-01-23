package com.rajat.minis3.dao;

import com.rajat.minis3.model.Bucket;

import java.util.List;
import java.util.Optional;

public interface BucketDao {

    List<Bucket> listBuckets();
    Optional<Bucket> getBucketDetails(String bucketName);
    boolean dropBucket(String bucketName);
    Optional<Bucket> createBucketFromName(String bucketName);

}
