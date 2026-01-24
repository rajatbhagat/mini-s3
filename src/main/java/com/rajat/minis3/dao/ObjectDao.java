package com.rajat.minis3.dao;

import com.rajat.minis3.config.OperationResult;
import com.rajat.minis3.model.Object;

import java.util.Optional;

public interface ObjectDao {
    Optional<Object> fetchObject(String bucketName, String objectKey);
    Optional<Object> uploadObject(String bucketName, String objectKey, byte[] fileContent);
    OperationResult deleteObject(String bucketName, String objectKey);
    Optional<Object> switchVersions(String bucketName, String objectKey, String versionId);
}
