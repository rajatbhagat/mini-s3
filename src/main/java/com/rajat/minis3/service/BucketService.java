package com.rajat.minis3.service;

import com.rajat.minis3.dao.BucketDao;
import com.rajat.minis3.model.Bucket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BucketService {

    private static final Logger logging = LogManager.getLogger(BucketService.class);

    @Autowired
    private BucketDao bucketDao;

    public List<Bucket> listBuckets() {
        return bucketDao.listBuckets();
    }


    public Optional<Bucket> getBucketDetails(String bucketName) {
        if (bucketDao.getBucketDetails(bucketName).isEmpty()) {
            logging.info("Bucket with {} already exists. Not re-creating it", bucketName);
            return Optional.empty();
        }
        try {
            logging.info("Fetching bucket details for bucket {}", bucketName);
            return bucketDao.getBucketDetails(bucketName);
        } catch (Exception e) {
            logging.info("Exception fetching bucket details for bucket {} : {}", bucketName, e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Bucket> createBucketIfNotExists(String bucketName) {
        Optional<Bucket> newBucket = bucketDao.getBucketDetails(bucketName);
        if (bucketDao.getBucketDetails(bucketName).isPresent()) {
            logging.info("Bucket with {} already exists. Not re-creating it", bucketName);
            return newBucket;
        }
        try {
            logging.info("Creating bucket with name {}", bucketName);
            newBucket = bucketDao.createBucketFromName(bucketName);
            logging.info("Bucket created");
            return newBucket;
        } catch (Exception e) {
            logging.error("Exception while creating bucket: {}", e.getMessage());
            return newBucket;
        }
    }

    public boolean dropBucketIfExists(String bucketName) {
        if (bucketDao.getBucketDetails(bucketName).isEmpty()) {
            logging.info("Bucket {} does not exist", bucketName);
            return false;
        }
        try {
            logging.info("Deleting bucket {}", bucketName);
            return bucketDao.dropBucket(bucketName);
        } catch (Exception e) {
            logging.error("Exception deleting bucket {} : {}", bucketName, e.getMessage());
            return false;
        }
    }
}
