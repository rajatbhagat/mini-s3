package com.rajat.minis3.controller;

import com.rajat.minis3.exception.MiniS3Exception;
import com.rajat.minis3.model.Object;
import com.rajat.minis3.service.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/objects")
public class ObjectController {

    @Autowired
    private ObjectService objectService;

    @GetMapping(value = "/list")
    public List<Object> listObjectDetails(@RequestParam String bucketName) {
        return objectService.listObjects(bucketName);
    }

    @GetMapping(value = "/details")
    public Object getObjectDetails(@RequestParam String bucketName, @RequestParam String objectKey) {
        return objectService.getObject(bucketName, objectKey);
    }

    @GetMapping(value = "/download")
    public ResponseEntity<?> downloadObject(@RequestParam String bucketName, @RequestParam String objectKey) {
        if (bucketName.isBlank() || objectKey.isBlank()) {
            return ResponseEntity.badRequest().body("Please provide valid bucket name and object key.");
        }

        String objectContent = objectService.getObjectContent(bucketName, objectKey);
        if (objectContent != null) {
            try {
                byte[] fileContent = Base64.getDecoder().decode(objectContent);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", Arrays.stream(objectKey.split("/"))
                        .collect(Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.isEmpty() ? null : list.get(list.size() - 1)
                        )));
                headers.setContentLength(fileContent.length);

                return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error retrieving file content: " + e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Object does not exist or has been deleted!");
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object uploadObject(
            @RequestParam String bucketName,
            @RequestParam String objectKey,
            @RequestParam("file") MultipartFile file
    ) throws MiniS3Exception {
        if (bucketName.isBlank() || objectKey.isBlank()) {
            throw new MiniS3Exception("Please provide valid bucket name and object key.");
        }
        if (file.isEmpty()) {
            throw new MiniS3Exception("Please provide a valid file.");
        }
        if (!file.getOriginalFilename().endsWith("txt")) {
            throw new MiniS3Exception("Invalid file type. Only text files are allowed");
        }
        if (file.getSize() > 1000000) {
            throw new MiniS3Exception(
                    "File size is too large!! This is just temporary project not meant for storing large sizes!!"
            );
        }
        return objectService.uploadObject(bucketName, objectKey, file);
    }

    @DeleteMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean deleteObject(
            @RequestParam String bucketName,
            @RequestParam String objectKey
    ) {
        return objectService.deleteObjectIfExists(bucketName, objectKey);
    }
}
