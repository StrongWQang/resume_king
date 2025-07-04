package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.Resume;
import com.example.resumebuilder.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    // 限流：记录每个IP最后一次请求时间
    private static final ConcurrentHashMap<String, Long> ipRequestTimeMap = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_INTERVAL_MS = 1000; // 1秒

    @PostMapping
    public ResponseEntity<String> saveResume(@RequestBody List<Map<String, Object>> resumeData, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        Long lastTime = ipRequestTimeMap.get(ip);
        if (lastTime != null && now - lastTime < RATE_LIMIT_INTERVAL_MS) {
            return ResponseEntity.status(429).body("请求过于频繁，请稍后再试");
        }
        ipRequestTimeMap.put(ip, now);
        try {
            String resumeId = resumeService.saveResume(resumeData);
            return ResponseEntity.ok(resumeId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResume(@PathVariable String id) {
        try {
            List<Map<String, Object>> resume = resumeService.getResume(id);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Resume>> getAllResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable String id) {
        try {
            resumeService.deleteResume(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<?> downloadPdf(@RequestBody List<Map<String, Object>> resumeData) {
        try {
            byte[] pdfBytes = resumeService.generatePdf(resumeData);
            return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=resume.pdf")
                .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 