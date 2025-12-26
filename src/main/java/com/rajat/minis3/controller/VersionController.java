package com.rajat.minis3.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/versions/")
public class VersionController {

    @GetMapping("/")
    public String health() {
        return "Hello World!";
    }
}
