package com.rajat.minis3.dao;

import com.rajat.minis3.config.OperationResult;
import com.rajat.minis3.model.Bucket;
import com.rajat.minis3.model.Object;
import com.rajat.minis3.repository.BucketRepository;
import com.rajat.minis3.repository.ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObjectDaoImpl implements ObjectDao{

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private ObjectRepository objectRepository;

    @Override
    public Optional<Object> fetchObject(String bucketName, String objectKey) {
        Bucket bucket = bucketRepository.findByName(bucketName).get();
        return objectRepository.findByBucketAndObjectKey(bucket, objectKey);
    }

    @Override
    public Optional<Object> uploadObject(String bucketName, String objectKey, byte[] fileContent) {
        Optional<Bucket> bucket = bucketRepository.findByName(bucketName);
        if (bucket.isEmpty()) {
            return Optional.empty();
        }
        return Optional.empty();
//        Optional<Object> object = Optional.of(
//                new Object(
//                    bucket.get(),
//                )
//        )
    }

    @Override
    public OperationResult deleteObject(String bucketName, String objectKey) {
        return null;
    }

    @Override
    public Optional<Object> switchVersions(String bucketName, String objectKey, String versionId) {
        return Optional.empty();
    }
}
