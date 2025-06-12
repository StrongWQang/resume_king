package com.example.resumebuilder.controller;

import com.example.resumebuilder.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @PostMapping
    public ResponseEntity<Map<String, String>> saveTemplate(@RequestBody List<Map<String, Object>> templateData) {
        String id = templateService.saveTemplate(templateData);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Map<String, Object>>> getTemplate(@PathVariable String id) {
        List<Map<String, Object>> templateData = templateService.getTemplate(id);
        return ResponseEntity.ok(templateData);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<List<Map<String, Object>>> exportTemplate(@PathVariable String id) {
        List<Map<String, Object>> templateData = templateService.exportTemplate(id);
        return ResponseEntity.ok(templateData);
    }

    @PostMapping("/export")
    public ResponseEntity<List<Map<String, Object>>> exportCurrentTemplate(@RequestBody List<Map<String, Object>> templateData) {
        return ResponseEntity.ok(templateData);
    }

    @PostMapping("/import")
    public ResponseEntity<List<Map<String, Object>>> importTemplate(@RequestParam("file") MultipartFile file) {
        List<Map<String, Object>> templateData = templateService.importTemplate(file);
        return ResponseEntity.ok(templateData);
    }
} 