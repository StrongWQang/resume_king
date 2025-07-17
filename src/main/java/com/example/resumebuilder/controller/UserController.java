package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.User;
import com.example.resumebuilder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String email = (String) request.get("email");
            String nickname = (String) request.get("nickname");
            
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
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邮箱不能为空"
                ));
            }
            
            Map<String, Object> result = userService.register(username, password, email, nickname);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
        try {
            String usernameOrEmail = (String) request.get("usernameOrEmail");
            String password = (String) request.get("password");
            
            // 参数验证
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户名或邮箱不能为空"
                ));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "密码不能为空"
                ));
            }
            
            Map<String, Object> result = userService.login(usernameOrEmail, password);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            Map<String, Object> result = userService.getUserInfo(userId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token,
                                         @RequestBody Map<String, Object> request) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            String nickname = (String) request.get("nickname");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String avatar = (String) request.get("avatar");
            
            Map<String, Object> result = userService.updateProfile(userId, nickname, email, phone, avatar);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token,
                                          @RequestBody Map<String, Object> request) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            String oldPassword = (String) request.get("oldPassword");
            String newPassword = (String) request.get("newPassword");
            
            // 参数验证
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "旧密码不能为空"
                ));
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "新密码不能为空"
                ));
            }
            
            Map<String, Object> result = userService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 验证用户权限
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateUser(@RequestHeader("Authorization") String token) {
        try {
            String userId = userService.validateToken(token);
            boolean isValid = userId != null && userService.validateUserPermission(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isValid", isValid,
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
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            // 简单的登出实现，实际应用中应该将token加入黑名单
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "登出成功"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户名不能为空"
                ));
            }
            
            boolean isAvailable = userService.searchUsers(username).isEmpty();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isAvailable", isAvailable,
                "message", isAvailable ? "用户名可用" : "用户名已存在"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "邮箱不能为空"
                ));
            }
            
            boolean isAvailable = userService.searchUsers(email).isEmpty();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isAvailable", isAvailable,
                "message", isAvailable ? "邮箱可用" : "邮箱已存在"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取用户列表（管理员功能）
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserList(@RequestHeader("Authorization") String token,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            // 检查管理员权限
            if (!userService.hasRole(userId, "ADMIN")) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            Map<String, Object> result = userService.getUserList(page, size);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 搜索用户（管理员功能）
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestHeader("Authorization") String token,
                                       @RequestParam String keyword) {
        try {
            String userId = userService.validateToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "用户未登录或Token无效"
                ));
            }
            
            // 检查管理员权限
            if (!userService.hasRole(userId, "ADMIN")) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "权限不足"
                ));
            }
            
            List<User> users = userService.searchUsers(keyword);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "users", users
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
} 