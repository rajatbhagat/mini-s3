package com.rajat.minis3.service;

import com.rajat.minis3.exception.MiniS3Exception;
import com.rajat.minis3.model.Bucket;
import com.rajat.minis3.model.Object;
import com.rajat.minis3.repository.BucketRepository;
import com.rajat.minis3.repository.ObjectRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Optional;

@Service
public class ObjectService {

    private static final Logger logging = LogManager.getLogger(ObjectService.class);

    @Autowired
    private ObjectRepository objectRepository;

    @Autowired
    private BucketRepository bucketRepository;

    @Transactional
    public String getObjectContent(String bucketName, String objectKey) {
        Optional<Bucket> bucketOpt = bucketRepository.findByName(bucketName);
        if (bucketOpt.isEmpty()) {
            logging.info("Bucket with name {} does not exist", bucketName);
            return null;
        }
        try {
            return objectRepository.findByBucketAndObjectKey(bucketOpt.get(), objectKey).get().getObjectContent();
        } catch (Exception e) {
            logging.error("Error retrieving object: {}", e.getMessage());
            return null;
        }
    }

    public Object uploadObject(String bucketName, String objectKey, MultipartFile file) throws MiniS3Exception {
        Optional<Bucket> bucketOpt = bucketRepository.findByName(bucketName);
        if (bucketOpt.isEmpty()) {
            logging.info("Bucket with name {} does not exist", bucketName);
            return null;
        }
        Bucket bucket = bucketOpt.get();
        try {
            logging.info(
                    "Saving file with name {} and size {} to bucket {}",
                    file.getName(), file.getSize(), bucket.getName()
            );
            String base64Content = Base64.getEncoder().encodeToString(file.getBytes());
            Object object = new Object();
            object.setBucket(bucket);
            object.setSize(file.getSize());
            object.setObjectKey(objectKey);
            object.setObjectContent(base64Content);
            object.setIsDeleted(false);
            objectRepository.save(object);
            logging.info("Object with key {} created in bucket {}", objectKey, bucketName);
            return object;
        } catch (Exception e) {
            logging.error("Error uploading object: {}", e.getMessage());
            throw new MiniS3Exception("Error uploading object", e);
        }
    }

    @Transactional
    public boolean deleteObjectIfExists(String bucketName, String objectKey) {
        Optional<Bucket> bucketOpt = bucketRepository.findByName(bucketName);
        if (bucketOpt.isEmpty()) {
            logging.info("Bucket with name {} does not exist", bucketName);
            return false;
        }

        Bucket bucket = bucketOpt.get();
        if (!objectRepository.existsByBucketAndObjectKey(bucket, objectKey)) {
            logging.info("Object with key {} does not exist in bucket {}", objectKey, bucketName);
            return false;
        }

        try {
            Optional<Object> storageObject = objectRepository.findByBucketAndObjectKey(bucket, objectKey);
            if (storageObject.isPresent()) {
                Object obj = storageObject.get();
                objectRepository.delete(obj);
                logging.info("Delete marker added for object with key {} in bucket {}", objectKey, bucketName);
                return true;
            }
            return false;
        } catch (Exception e) {
            logging.error("Error deleting object: {}", e.getMessage());
            return false;
        }
    }
}
