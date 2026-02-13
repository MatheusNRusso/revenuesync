package com.mtnrs.revenuesync.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock")
public class MockController {

    @PostMapping(value = "/google", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> google(@RequestBody(required = false) String body) {
        return ResponseEntity.ok("""
            {"status":"OK","provider":"GOOGLE","mode":"MOCK"}
        """);
    }

    @PostMapping(value = "/meta", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> meta(@RequestBody(required = false) String body) {
        return ResponseEntity.ok("""
            {"status":"OK","provider":"META","mode":"MOCK"}
        """);
    }

    @PostMapping(value = "/pipedrive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> pipedrive(@RequestBody(required = false) String body) {
        return ResponseEntity.ok("""
            {"status":"OK","provider":"PIPEDRIVE","mode":"MOCK"}
        """);
    }
}
