package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.Resume;
import com.example.resumebuilder.service.ResumeService;
import com.example.resumebuilder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private UserService userService;

    // 限流：记录每个IP最后一次请求时间
    private static final ConcurrentHashMap<String, Long> ipRequestTimeMap = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_INTERVAL_MS = 1000; // 1秒

    @PostMapping
    public ResponseEntity<?> saveResume(@RequestBody List<Map<String, Object>> resumeData, 
                                       @RequestHeader(value = "Authorization", required = false) String token,
                                       HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        Long lastTime = ipRequestTimeMap.get(ip);
        if (lastTime != null && now - lastTime < RATE_LIMIT_INTERVAL_MS) {
            return ResponseEntity.status(429).body(Map.of(
                "success", false,
                "message", "请求过于频繁，请稍后再试"
            ));
        }
        ipRequestTimeMap.put(ip, now);
        
        try {
            // 验证用户登录状态
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "保存简历需要登录，请先登录"
                ));
            }
            
            String resumeId = resumeService.saveResume(resumeData, userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "保存成功",
                "resumeId", resumeId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // 用于导入导出的保存方法（不需要登录）
    @PostMapping("/temporary")
    public ResponseEntity<?> saveTemporaryResume(@RequestBody List<Map<String, Object>> resumeData, 
                                                HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        Long lastTime = ipRequestTimeMap.get(ip);
        if (lastTime != null && now - lastTime < RATE_LIMIT_INTERVAL_MS) {
            return ResponseEntity.status(429).body(Map.of(
                "success", false,
                "message", "请求过于频繁，请稍后再试"
            ));
        }
        ipRequestTimeMap.put(ip, now);
        
        try {
            String resumeId = resumeService.saveResumeWithoutLogin(resumeData);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "临时保存成功",
                "resumeId", resumeId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResume(@PathVariable String id) {
        try {
            List<Map<String, Object>> resume = resumeService.getResume(id);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取用户的简历列表
     */
    @GetMapping("/user/list")
    public ResponseEntity<?> getUserResumes(@RequestHeader("Authorization") String token) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            List<Resume> resumes = resumeService.getUserResumes(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "resumes", resumes
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 更新简历
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateResume(@PathVariable Long id,
                                        @RequestBody Map<String, Object> request,
                                        @RequestHeader("Authorization") String token) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resumeData = (List<Map<String, Object>>) request.get("resumeData");
            String title = (String) request.get("title");
            
            resumeService.updateResume(id, userId, resumeData, title);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "更新成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 删除简历
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            resumeService.deleteResume(id, userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
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

    // 删除此方法，避免与/user/list路径的getUserResumes方法重复
    // 用户只能通过Token获取自己的简历列表，不能直接通过userId获取其他用户的简历

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
            @PathVariable Long id,
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