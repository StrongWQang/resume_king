package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.ResumePublishRequest;
import com.example.resumebuilder.service.ResumePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume-publish")
@CrossOrigin(origins = "*")
public class ResumePublishController {
    
    @Autowired
    private ResumePublishService publishService;
    
    /**
     * 提交简历发布申请
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitPublishRequest(@RequestBody Map<String, Object> request) {
        try {
            String resumeIdStr = (String) request.get("resumeId");
            String userId = (String) request.get("userId");
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            
            // 参数验证
            if (resumeIdStr == null || resumeIdStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("简历ID不能为空");
            }
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("标题不能为空");
            }
            
            // 转换resumeId为Long类型
            Long resumeId;
            try {
                resumeId = Long.parseLong(resumeIdStr);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("简历ID格式不正确");
            }
            
            String requestId = publishService.submitPublishRequest(resumeId, userId, title, description);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "申请提交成功",
                "requestId", requestId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取申请详情
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequestDetail(@PathVariable String requestId) {
        try {
            ResumePublishRequest request = publishService.getRequestDetail(requestId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取申请列表（分页）
     */
    @GetMapping("/list")
    public ResponseEntity<?> getRequestList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            if (page < 1) {
                return ResponseEntity.badRequest().body("页码必须大于0");
            }
            if (size < 1 || size > 100) {
                return ResponseEntity.badRequest().body("每页数量必须在1-100之间");
            }
            
            Map<String, Object> result = publishService.getRequestList(page, size, status);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取待审批申请列表
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests() {
        try {
            List<ResumePublishRequest> requests = publishService.getPendingRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取用户的申请记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRequests(@PathVariable String userId) {
        try {
            List<ResumePublishRequest> requests = publishService.getUserRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 取消申请
     */
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelRequest(
            @PathVariable String requestId,
            @RequestParam(required = false) String userId) {
        try {
            publishService.cancelRequest(requestId, userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "申请取消成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取申请状态统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getRequestStatistics() {
        try {
            Map<String, Object> statistics = publishService.getRequestStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取审批工作台数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getApprovalDashboard() {
        try {
            List<Map<String, Object>> dashboard = publishService.getApprovalDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
} 