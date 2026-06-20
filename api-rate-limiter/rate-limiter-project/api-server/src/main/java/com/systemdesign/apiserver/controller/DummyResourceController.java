package com.systemdesign.apiserver.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DummyResourceController {

    @GetMapping("/data")
    public ResponseEntity<Map<String, String>> getDummyData() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Request reached the API server successfully! You were not rate limited."
        ));
    }
}
