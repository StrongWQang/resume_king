package com.example.resumebuilder.controller;

import com.example.resumebuilder.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/optimize")
    public ResponseEntity<?> optimizeText(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String optimizedText = aiService.optimizeText(text);
            return ResponseEntity.ok(Map.of("optimizedText", optimizedText));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 