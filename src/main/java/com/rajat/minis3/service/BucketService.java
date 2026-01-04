package com.rajat.minis3.service;

import com.rajat.minis3.config.OperationResult;
import com.rajat.minis3.model.Bucket;
import com.rajat.minis3.repository.BucketRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BucketService {

    private static final Logger logging = LogManager.getLogger(BucketService.class);

    @Autowired
    private BucketRepository bucketRepository;

    public OperationResult createBucketIfNotExists(String bucketName) {
        if (bucketRepository.existsByName(bucketName)) {
            logging.info("Bucket with %s already exists. Not re-creating it");
            return OperationResult.BUCKET_ALREADY_EXISTS;
        }
        try {
            Bucket bucket = new Bucket(bucketName);
            bucketRepository.save(bucket);
            return OperationResult.BUCKET_CREATED;
        } catch (Exception e) {
            return OperationResult.ERROR;
        }
    }
}
