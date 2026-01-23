package com.rajat.minis3.controller;

import com.rajat.minis3.model.Bucket;
import com.rajat.minis3.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bucket")
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Bucket> listBuckets() {
        return bucketService.listBuckets();
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Bucket getBucketDetails(@RequestParam String bucketName) {
        if (bucketName.isBlank()) {
            ResponseEntity.badRequest().body("Please provide a valid bucket name.");
        }
        Optional<Bucket> bucket = bucketService.getBucketDetails(bucketName);
        return bucket.orElse(null);
    }

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Bucket createBucket (@RequestParam String bucketName) {
        if (bucketName.isBlank()) {
            ResponseEntity.badRequest().body("Please provide a valid bucket name.");
        }
        Optional<Bucket> bucket = bucketService.createBucketIfNotExists(bucketName);
        return bucket.orElse(null);
    }


    @DeleteMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean deleteBucket (@RequestParam String bucketName) {
        if (bucketName.isBlank()) {
            ResponseEntity.badRequest().body("Please provide a valid bucket name.");
        }
        return bucketService.dropBucketIfExists(bucketName);
    }

}
