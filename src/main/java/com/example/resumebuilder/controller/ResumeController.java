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

    @GetMapping("/popular")
    public ResponseEntity<List<Resume>> getPopularResumes(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(resumeService.getPopularResumes(limit));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Resume>> getUserResumes(@PathVariable String userId) {
        return ResponseEntity.ok(resumeService.getResumesByUserId(userId));
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        if (page < 1) {
            return ResponseEntity.badRequest().body("页码必须大于0");
        }
        if (size < 1 || size > 50) {
            return ResponseEntity.badRequest().body("每页数量必须在1-50之间");
        }
        return ResponseEntity.ok(resumeService.getTemplatesWithPagination(page, size));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateResumeStatus(
            @PathVariable String id,
            @RequestBody Map<String, Integer> statusUpdate) {
        try {
            Integer status = statusUpdate.get("status");
            if (status == null || (status < 0 || status > 3)) {
                return ResponseEntity.badRequest().body("无效的状态值");
            }
            resumeService.updateResumeStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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