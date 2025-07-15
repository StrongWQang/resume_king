package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.AdminUser;
import com.example.resumebuilder.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private AdminUserService adminUserService;
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户名不能为空"
                ));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "密码不能为空"
                ));
            }
            
            Map<String, Object> result = adminUserService.authenticate(username, password);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 验证管理员权限
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateAdmin(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户ID不能为空"
                ));
            }
            
            boolean isValid = adminUserService.validateAdminPermission(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isValid", isValid
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取管理员用户信息
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable String userId) {
        try {
            AdminUser user = adminUserService.getUserInfo(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取活跃管理员用户列表
     */
    @GetMapping("/users/active")
    public ResponseEntity<?> getActiveUsers() {
        try {
            List<AdminUser> users = adminUserService.getActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取管理员用户列表（分页）
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (page < 1) {
                return ResponseEntity.badRequest().body("页码必须大于0");
            }
            if (size < 1 || size > 100) {
                return ResponseEntity.badRequest().body("每页数量必须在1-100之间");
            }
            
            Map<String, Object> result = adminUserService.getUserList(page, size);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 搜索管理员用户
     */
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("搜索关键词不能为空");
            }
            
            List<AdminUser> users = adminUserService.searchUsers(keyword);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 创建管理员用户
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String email = (String) request.get("email");
            
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("密码不能为空");
            }
            
            String userId = adminUserService.createAdminUser(username, password, email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "管理员用户创建成功",
                "userId", userId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 更新管理员密码
     */
    @PutMapping("/users/{userId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            String oldPassword = (String) request.get("oldPassword");
            String newPassword = (String) request.get("newPassword");
            
            // 参数验证
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("旧密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("新密码不能为空");
            }
            
            adminUserService.updatePassword(userId, oldPassword, newPassword);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "密码更新成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 更新管理员邮箱
     */
    @PutMapping("/users/{userId}/email")
    public ResponseEntity<?> updateEmail(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            String newEmail = (String) request.get("email");
            
            // 参数验证
            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("邮箱不能为空");
            }
            
            adminUserService.updateEmail(userId, newEmail);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "邮箱更新成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 禁用管理员用户
     */
    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable String userId) {
        try {
            adminUserService.deactivateUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户禁用成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 激活管理员用户
     */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable String userId) {
        try {
            adminUserService.activateUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户激活成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 重置管理员密码
     */
    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String userId) {
        try {
            String newPassword = adminUserService.resetPassword(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "密码重置成功",
                "newPassword", newPassword
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 删除管理员用户
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            adminUserService.deleteUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户删除成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取管理员审批统计
     */
    @GetMapping("/statistics/approval")
    public ResponseEntity<?> getApprovalStatistics() {
        try {
            List<Map<String, Object>> statistics = adminUserService.getApprovalStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 验证管理员密码（用于敏感操作）
     */
    @PostMapping("/users/{userId}/validate-password")
    public ResponseEntity<?> validatePassword(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            String password = (String) request.get("password");
            
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "密码不能为空"
                ));
            }
            
            boolean isValid = adminUserService.validatePassword(userId, password);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isValid", isValid
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
} 