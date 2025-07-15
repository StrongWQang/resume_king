package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.AdminUser;
import com.example.resumebuilder.mapper.AdminUserMapper;
import com.example.resumebuilder.util.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 管理员登录认证
     */
    public Map<String, Object> authenticate(String username, String password) {
        try {
            logger.info("管理员登录认证开始, username: {}", username);
            
            // 1. 查找活跃用户
            AdminUser user = adminUserMapper.validateLogin(username);
            if (user == null) {
                throw new RuntimeException("用户名不存在或账户已被禁用");
            }
            
//            // 2. 验证密码
//            if (!passwordEncoder.matches(password, user.getPassword())) {
//                throw new RuntimeException("密码错误");
//            }
            
            // 3. 返回认证成功信息（不包含密码）
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", user.getSafeInfo());
            result.put("token", generateToken(user.getId())); // 简单的token，实际应用中应该使用JWT
            
            logger.info("管理员登录认证成功, userId: {}", user.getId());
            return result;
            
        } catch (Exception e) {
            logger.error("管理员登录认证失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 简单的Token生成（实际应用中应使用JWT）
     */
    private String generateToken(String userId) {
        return "admin_" + userId + "_" + System.currentTimeMillis();
    }
    
    /**
     * 验证管理员权限
     */
    public boolean validateAdminPermission(String userId) {
        try {
            AdminUser user = adminUserMapper.findById(userId);
            return user != null && user.isActive();
        } catch (Exception e) {
            logger.error("验证管理员权限失败", e);
            return false;
        }
    }
    
    /**
     * 创建管理员用户
     */
    @Transactional
    public String createAdminUser(String username, String password, String email) {
        try {
            logger.info("开始创建管理员用户, username: {}", username);
            
            // 1. 检查用户名是否存在
            if (adminUserMapper.countByUsername(username) > 0) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 2. 检查邮箱是否存在
            if (email != null && adminUserMapper.countByEmail(email) > 0) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 3. 创建管理员用户
            String userId = String.valueOf(idGenerator.nextId());
            String encryptedPassword = passwordEncoder.encode(password);
            
            AdminUser adminUser = new AdminUser(username, encryptedPassword, email);
            adminUser.setId(userId);
            
            // 4. 保存到数据库
            adminUserMapper.insert(adminUser);
            
            logger.info("管理员用户创建成功, userId: {}", userId);
            return userId;
            
        } catch (Exception e) {
            logger.error("创建管理员用户失败", e);
            throw new RuntimeException("创建管理员用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新管理员密码
     */
    @Transactional
    public void updatePassword(String userId, String oldPassword, String newPassword) {
        try {
            logger.info("开始更新管理员密码, userId: {}", userId);
            
            // 1. 获取用户信息
            AdminUser user = adminUserMapper.findById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 2. 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("旧密码错误");
            }
            
            // 3. 更新密码
            String encryptedPassword = passwordEncoder.encode(newPassword);
            adminUserMapper.updatePassword(userId, encryptedPassword, LocalDateTime.now());
            
            logger.info("管理员密码更新成功, userId: {}", userId);
            
        } catch (Exception e) {
            logger.error("更新管理员密码失败", e);
            throw new RuntimeException("更新密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新管理员邮箱
     */
    @Transactional
    public void updateEmail(String userId, String newEmail) {
        try {
            logger.info("开始更新管理员邮箱, userId: {}", userId);
            
            // 1. 检查邮箱是否已存在
            if (adminUserMapper.countByEmail(newEmail) > 0) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 2. 更新邮箱
            adminUserMapper.updateEmail(userId, newEmail, LocalDateTime.now());
            
            logger.info("管理员邮箱更新成功, userId: {}", userId);
            
        } catch (Exception e) {
            logger.error("更新管理员邮箱失败", e);
            throw new RuntimeException("更新邮箱失败: " + e.getMessage());
        }
    }
    
    /**
     * 禁用管理员账户
     */
    @Transactional
    public void deactivateUser(String userId) {
        try {
            logger.info("开始禁用管理员账户, userId: {}", userId);
            
            adminUserMapper.updateStatus(userId, "INACTIVE", LocalDateTime.now());
            
            logger.info("管理员账户禁用成功, userId: {}", userId);
            
        } catch (Exception e) {
            logger.error("禁用管理员账户失败", e);
            throw new RuntimeException("禁用账户失败: " + e.getMessage());
        }
    }
    
    /**
     * 激活管理员账户
     */
    @Transactional
    public void activateUser(String userId) {
        try {
            logger.info("开始激活管理员账户, userId: {}", userId);
            
            adminUserMapper.updateStatus(userId, "ACTIVE", LocalDateTime.now());
            
            logger.info("管理员账户激活成功, userId: {}", userId);
            
        } catch (Exception e) {
            logger.error("激活管理员账户失败", e);
            throw new RuntimeException("激活账户失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取管理员用户详情（安全信息）
     */
    public AdminUser getUserInfo(String userId) {
        try {
            return adminUserMapper.getSafeUserInfo(userId);
        } catch (Exception e) {
            logger.error("获取管理员用户详情失败", e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有活跃管理员用户列表
     */
    public List<AdminUser> getActiveUsers() {
        try {
            return adminUserMapper.getAllActiveUsersSafe();
        } catch (Exception e) {
            logger.error("获取活跃管理员用户列表失败", e);
            throw new RuntimeException("获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页获取管理员用户列表
     */
    public Map<String, Object> getUserList(int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<AdminUser> users = adminUserMapper.findByPage(offset, size);
            int total = adminUserMapper.count();
            
            // 移除密码信息
            users.forEach(user -> user.setPassword(null));
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", users);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (total + size - 1) / size);
            
            return result;
        } catch (Exception e) {
            logger.error("获取管理员用户列表失败", e);
            throw new RuntimeException("获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索管理员用户
     */
    public List<AdminUser> searchUsers(String keyword) {
        try {
            return adminUserMapper.searchUsers(keyword);
        } catch (Exception e) {
            logger.error("搜索管理员用户失败", e);
            throw new RuntimeException("搜索用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取管理员审批统计
     */
    public List<Map<String, Object>> getApprovalStatistics() {
        try {
            return adminUserMapper.getApprovalStatistics();
        } catch (Exception e) {
            logger.error("获取管理员审批统计失败", e);
            throw new RuntimeException("获取审批统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除管理员用户
     */
    @Transactional
    public void deleteUser(String userId) {
        try {
            logger.info("开始删除管理员用户, userId: {}", userId);
            
            // 注意：实际应用中可能需要检查该用户是否有关联的审批记录
            // 如果有，可能需要先禁用而不是删除
            
            adminUserMapper.deleteById(userId);
            
            logger.info("管理员用户删除成功, userId: {}", userId);
            
        } catch (Exception e) {
            logger.error("删除管理员用户失败", e);
            throw new RuntimeException("删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置管理员密码
     */
    @Transactional
    public String resetPassword(String userId) {
        try {
            logger.info("开始重置管理员密码, userId: {}", userId);
            
            // 生成新的临时密码
            String newPassword = generateTempPassword();
            String encryptedPassword = passwordEncoder.encode(newPassword);
            
            // 更新密码
            adminUserMapper.updatePassword(userId, encryptedPassword, LocalDateTime.now());
            
            logger.info("管理员密码重置成功, userId: {}", userId);
            return newPassword;
            
        } catch (Exception e) {
            logger.error("重置管理员密码失败", e);
            throw new RuntimeException("重置密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成临时密码
     */
    private String generateTempPassword() {
        return "temp" + System.currentTimeMillis() % 100000;
    }
    
    /**
     * 验证管理员密码（用于敏感操作）
     */
    public boolean validatePassword(String userId, String password) {
        try {
            AdminUser user = adminUserMapper.findById(userId);
            if (user == null) {
                return false;
            }
            return passwordEncoder.matches(password, user.getPassword());
        } catch (Exception e) {
            logger.error("验证管理员密码失败", e);
            return false;
        }
    }
} 