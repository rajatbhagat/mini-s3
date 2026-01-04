package com.rajat.minis3.config;

import lombok.Getter;

@Getter
public enum OperationResult {

    // Bucket Related Operation Results
    BUCKET_ALREADY_EXISTS("Already Exists"),
    BUCKET_CREATED("Bucket Created!"),
    BUCKET_DROPPED("Bucket Dropped!"),

    // Object Related Operation Results
    OBJECT_UPLOADED("File uploaded"),
    OBJECT_MODIFIED("File modified"),
    OBJECT_DELETED("File deleted"),

    // Default Error
    ERROR("Internal Error");

    private final String operationResult;

    OperationResult(String roomType) {
        this.operationResult = roomType;
    }

}
