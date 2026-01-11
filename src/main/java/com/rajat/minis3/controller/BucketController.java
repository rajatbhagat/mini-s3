package com.rajat.minis3.controller;

import com.rajat.minis3.config.OperationResult;
import com.rajat.minis3.model.Bucket;
import com.rajat.minis3.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/bucket")
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getBucketDetails(
            @RequestParam String bucketName
    ) {
        if (bucketName.isBlank()) {
            ResponseEntity.badRequest().body("Please provide a valid bucket name.");
        }
        Optional<Bucket> bucket = bucketService.getBucketDetails(bucketName);
        if (bucket.isPresent()) {
            return bucket.get().toString();
        }
        return OperationResult.BUCKET_DOES_NOT_EXIST.getOperationResult();
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createBucket (
            @RequestParam String bucketName
    ) {
        if (bucketName.isBlank()) {
            ResponseEntity.badRequest().body("Please provide a valid bucket name.");
        }
        Optional<Bucket> bucket = bucketService.createBucketIfNotExists(bucketName);
        if(bucket.isEmpty()) {
            return OperationResult.ERROR.getOperationResult();
        }
        return bucket.get().toString();
    }


    @DeleteMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String deleteBucket (
            @RequestParam String bucketName
    ) {
        if (bucketName.isBlank()) {
            ResponseEntity.badRequest().body("Please provide a valid bucket name.");
        }
        return bucketService.dropBucketIfExists(bucketName).getOperationResult();
    }

}
