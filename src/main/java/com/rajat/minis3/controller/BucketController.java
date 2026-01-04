package com.rajat.minis3.controller;

import com.rajat.minis3.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bucket/")
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @GetMapping("/")
    public String health() {
        return "Hello World";
    }

    @PostMapping("/createBucket")
    public String createBucket (@RequestParam String bucketName) {
        if (bucketName.isBlank()) {
            return "Please provide a valid bucket name.";
        }
        return bucketService.createBucketIfNotExists(bucketName).getOperationResult();
    }

}
