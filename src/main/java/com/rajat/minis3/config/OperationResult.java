package com.rajat.minis3.config;

import lombok.Getter;

@Getter
public enum OperationResult {

    // Bucket Related Operation Results
    BUCKET_ALREADY_EXISTS("Already Exists"),
    BUCKET_CREATED("Bucket Created!"),
    BUCKET_DROPPED("Bucket Dropped!"),
    BUCKET_DOES_NOT_EXIST("Bucket Does not exist!"),

    // Object Related Operation Results
    OBJECT_UPLOADED("File uploaded"),
    OBJECT_MODIFIED("File modified"),
    OBJECT_DELETED("File deleted"),

    // Version Related Operation Results
    VERSION_CREATED("Version created"),
    VERSION_DELETED("Version deleted"),
    VERSION_DOES_NOT_EXIST("Version does not exist"),

    // Default Error
    ERROR("Internal Error");

    private final String operationResult;

    OperationResult(String roomType) {
        this.operationResult = roomType;
    }

}
