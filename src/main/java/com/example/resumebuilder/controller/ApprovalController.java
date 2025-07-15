package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.ApprovalRecord;
import com.example.resumebuilder.mapper.ApprovalRecordMapper;
import com.example.resumebuilder.service.AdminUserService;
import com.example.resumebuilder.service.ResumePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approval")
@CrossOrigin(origins = "*")
public class ApprovalController {
    
    @Autowired
    private ResumePublishService publishService;
    
    @Autowired
    private AdminUserService adminUserService;
    
    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;
    
    /**
     * 审批通过申请
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable String requestId,
            @RequestBody Map<String, Object> request) {
        try {
            String approverId = (String) request.get("approverId");
            String comment = (String) request.get("comment");
            
            // 参数验证
            if (approverId == null || approverId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("审批人ID不能为空");
            }
            
            // 验证管理员权限
            if (!adminUserService.validateAdminPermission(approverId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "无权限进行审批操作"
                ));
            }
            
            publishService.approveRequest(requestId, approverId, comment);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审批通过成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 拒绝申请
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable String requestId,
            @RequestBody Map<String, Object> request) {
        try {
            String approverId = (String) request.get("approverId");
            String reason = (String) request.get("reason");
            
            // 参数验证
            if (approverId == null || approverId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("审批人ID不能为空");
            }
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("拒绝理由不能为空");
            }
            
            // 验证管理员权限
            if (!adminUserService.validateAdminPermission(approverId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "无权限进行审批操作"
                ));
            }
            
            publishService.rejectRequest(requestId, approverId, reason);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "申请拒绝成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 批量审批
     */
    @PostMapping("/batch")
    public ResponseEntity<?> batchApprove(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> requestIds = (List<String>) request.get("requestIds");
            String approverId = (String) request.get("approverId");
            String action = (String) request.get("action"); // "approve" or "reject"
            String comment = (String) request.get("comment");
            
            // 参数验证
            if (requestIds == null || requestIds.isEmpty()) {
                return ResponseEntity.badRequest().body("申请ID列表不能为空");
            }
            if (approverId == null || approverId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("审批人ID不能为空");
            }
            if (!"approve".equals(action) && !"reject".equals(action)) {
                return ResponseEntity.badRequest().body("操作类型必须是approve或reject");
            }
            
            // 验证管理员权限
            if (!adminUserService.validateAdminPermission(approverId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "无权限进行审批操作"
                ));
            }
            
            publishService.batchApprove(requestIds, approverId, action, comment);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量审批成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取审批历史记录
     */
    @GetMapping("/{requestId}/history")
    public ResponseEntity<?> getApprovalHistory(@PathVariable String requestId) {
        try {
            List<Map<String, Object>> history = approvalRecordMapper.getApprovalHistory(Long.valueOf(requestId));
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取审批记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<?> getApprovalRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String approverId,
            @RequestParam(required = false) String operation) {
        try {
            if (page < 1) {
                return ResponseEntity.badRequest().body("页码必须大于0");
            }
            if (size < 1 || size > 100) {
                return ResponseEntity.badRequest().body("每页数量必须在1-100之间");
            }
            
            int offset = (page - 1) * size;
            List<ApprovalRecord> records;
            int total;
            
            if (approverId != null && !approverId.trim().isEmpty()) {
                records = approvalRecordMapper.findByApproverId(approverId);
                total = approvalRecordMapper.countByApproverId(approverId);
            } else if (operation != null && !operation.trim().isEmpty()) {
                records = approvalRecordMapper.findByOperation(operation);
                // 这里需要添加按操作类型计数的方法
                total = approvalRecordMapper.count();
            } else {
                records = approvalRecordMapper.findByPage(offset, size);
                total = approvalRecordMapper.count();
            }
            
            Map<String, Object> result = Map.of(
                "records", records,
                "total", total,
                "page", page,
                "size", size,
                "totalPages", (total + size - 1) / size
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取审批统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getApprovalStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = startTime != null ? 
                LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endTime != null ? 
                LocalDateTime.parse(endTime) : LocalDateTime.now();
            
            List<Map<String, Object>> statistics = approvalRecordMapper.getOperationStatistics(start, end);
            
            return ResponseEntity.ok(Map.of(
                "statistics", statistics,
                "startTime", start,
                "endTime", end
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取审批人工作量统计
     */
    @GetMapping("/workload")
    public ResponseEntity<?> getApproverWorkload(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = startTime != null ? 
                LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endTime != null ? 
                LocalDateTime.parse(endTime) : LocalDateTime.now();
            
            List<Map<String, Object>> workload = approvalRecordMapper.getApproverWorkload(start, end);
            
            return ResponseEntity.ok(Map.of(
                "workload", workload,
                "startTime", start,
                "endTime", end
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取最近的审批记录
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentApprovalRecords(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit < 1 || limit > 100) {
                return ResponseEntity.badRequest().body("限制数量必须在1-100之间");
            }
            
            List<Map<String, Object>> records = approvalRecordMapper.getRecentApprovalRecords(limit);
            return ResponseEntity.ok(records);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
} 