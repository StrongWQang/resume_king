package com.example.resumebuilder.controller;

import com.example.resumebuilder.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping
    public ResponseEntity<Map<String, String>> saveResume(@RequestBody List<Map<String, Object>> resumeData) {
        String id = resumeService.saveResume(resumeData);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Map<String, Object>>> getResume(@PathVariable String id) {
        List<Map<String, Object>> resumeData = resumeService.getResume(id);
        return ResponseEntity.ok(resumeData);
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody List<Map<String, Object>> resumeData) {
        byte[] pdfBytes = resumeService.generatePdf(resumeData);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=resume.pdf")
                .body(pdfBytes);
    }
} 