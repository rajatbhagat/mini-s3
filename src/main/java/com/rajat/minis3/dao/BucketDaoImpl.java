package com.rajat.minis3.dao;

import com.rajat.minis3.model.Bucket;
import com.rajat.minis3.repository.BucketRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BucketDaoImpl implements BucketDao{

    private static final Logger logging = LogManager.getLogger(BucketDaoImpl.class);

    @Autowired
    private BucketRepository bucketRepository;

    @Override
    public List<Bucket> listBuckets() {
        return bucketRepository.findAll();
    }

    @Override
    public Optional<Bucket> getBucketDetails(String bucketName) {
        return bucketRepository.findByName(bucketName);
    }

    @Override
    public boolean dropBucket(String bucketName) {
        Optional<Bucket> bucket = getBucketDetails(bucketName);
        try {
            bucketRepository.delete(bucket.get());
            return true;
        } catch (Exception e) {
            logging.error("Exception while deleting bucket {} : {}", bucket, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Bucket> createBucketFromName(String bucketName) {
        if(!bucketRepository.existsByName(bucketName)) {
            try {
                Bucket bucket = new Bucket(bucketName);
                return Optional.of(bucketRepository.save(bucket));
            } catch (Exception e) {
                logging.error("Exception while creating bucket {} : {}", bucketName, e.getMessage());
            }
        }
        return Optional.empty();
    }
}
