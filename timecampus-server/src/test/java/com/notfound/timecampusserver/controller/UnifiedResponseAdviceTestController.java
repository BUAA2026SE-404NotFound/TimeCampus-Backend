package com.notfound.timecampusserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UnifiedResponseAdviceTestController {

    @GetMapping("/test-unified-response")
    Map<String, Object> test() {
        return Map.of("status", "UP");
    }
}
