package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.User;
import com.example.resumebuilder.entity.UserRole;
import com.example.resumebuilder.mapper.UserMapper;
import com.example.resumebuilder.mapper.UserRoleMapper;
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
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户注册
     */
    @Transactional
    public Map<String, Object> register(String username, String password, String email, String nickname) {
        try {
            logger.info("用户注册开始, username: {}, email: {}", username, email);
            
            // 1. 参数验证
            if (username == null || username.trim().isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            if (password == null || password.length() < 6) {
                throw new RuntimeException("密码长度不能少于6位");
            }
            if (email == null || !email.contains("@")) {
                throw new RuntimeException("邮箱格式不正确");
            }
            
            // 2. 检查用户名是否存在
            if (userMapper.countByUsername(username) > 0) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 3. 检查邮箱是否存在
            if (userMapper.countByEmail(email) > 0) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 4. 创建用户
            String userId = String.valueOf(idGenerator.nextId());
            String encryptedPassword = passwordEncoder.encode(password);
            
            User user = new User(username, encryptedPassword, email, nickname != null ? nickname : username);
            user.setId(userId);
            
            // 5. 保存用户到数据库
            userMapper.insert(user);
            
            // 6. 分配默认角色
            UserRole userRole = new UserRole(userId, UserRole.Role.USER);
            userRole.setId(String.valueOf(idGenerator.nextId()));
            userRoleMapper.insert(userRole);
            
            // 7. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("user", user.getSafeInfo());
            result.put("token", generateToken(userId));
            
            logger.info("用户注册成功, userId: {}", userId);
            return result;
            
        } catch (Exception e) {
            logger.error("用户注册失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String usernameOrEmail, String password) {
        try {
            logger.info("用户登录开始, usernameOrEmail: {}", usernameOrEmail);
            
            // 1. 查找用户（支持用户名或邮箱登录）
            User user = null;
            if (usernameOrEmail.contains("@")) {
                user = userMapper.validateLoginByEmail(usernameOrEmail);
            } else {
                user = userMapper.validateLogin(usernameOrEmail);
            }
            
            if (user == null) {
                throw new RuntimeException("用户名或邮箱不存在或账户已被禁用");
            }
            
            // 2. 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("密码错误");
            }
            
            // 3. 更新登录时间
            user.updateLastLoginTime();
            userMapper.updateLastLoginTime(user.getId(), user.getLastLoginTime(), 
                                         user.getLoginCount(), user.getUpdateTime());
            
            // 4. 获取用户角色
            List<String> roles = userRoleMapper.getRolesByUserId(user.getId());
            
            // 5. 返回登录成功信息
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user.getSafeInfo());
            result.put("roles", roles);
            result.put("token", generateToken(user.getId()));
            
            logger.info("用户登录成功, userId: {}", user.getId());
            return result;
            
        } catch (Exception e) {
            logger.error("用户登录失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取用户信息
     */
    public Map<String, Object> getUserInfo(String userId) {
        try {
            User user = userMapper.getSafeUserInfo(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 获取用户角色
            List<String> roles = userRoleMapper.getRolesByUserId(userId);
            
            // 获取用户简历统计
            List<Integer> resumeCounts = userMapper.getUserResumeCount(userId);
            int totalResumeCount = resumeCounts.stream().mapToInt(Integer::intValue).sum();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", user.getSafeInfo());
            result.put("roles", roles);
            result.put("resumeCount", totalResumeCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public Map<String, Object> updateProfile(String userId, String nickname, String email, String phone, String avatar) {
        try {
            logger.info("更新用户信息开始, userId: {}", userId);
            
            // 1. 获取用户信息
            User user = userMapper.findById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 2. 检查邮箱是否被其他用户使用
            if (email != null && !email.equals(user.getEmail())) {
                if (userMapper.countByEmail(email) > 0) {
                    throw new RuntimeException("邮箱已被其他用户使用");
                }
            }
            
            // 3. 更新用户信息
            user.updateProfile(nickname, email, phone, avatar);
            userMapper.update(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("user", user.getSafeInfo());
            
            logger.info("用户信息更新成功, userId: {}", userId);
            return result;
            
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public Map<String, Object> changePassword(String userId, String oldPassword, String newPassword) {
        try {
            logger.info("修改密码开始, userId: {}", userId);
            
            // 1. 获取用户信息
            User user = userMapper.findById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 2. 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("旧密码错误");
            }
            
            // 3. 验证新密码
            if (newPassword == null || newPassword.length() < 6) {
                throw new RuntimeException("新密码长度不能少于6位");
            }
            
            // 4. 更新密码
            String encryptedPassword = passwordEncoder.encode(newPassword);
            userMapper.updatePassword(userId, encryptedPassword, LocalDateTime.now());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "密码修改成功");
            
            logger.info("密码修改成功, userId: {}", userId);
            return result;
            
        } catch (Exception e) {
            logger.error("修改密码失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 验证用户权限
     */
    public boolean validateUserPermission(String userId) {
        try {
            User user = userMapper.findById(userId);
            return user != null && user.isActive();
        } catch (Exception e) {
            logger.error("验证用户权限失败", e);
            return false;
        }
    }
    
    /**
     * 检查用户是否具有某个角色
     */
    public boolean hasRole(String userId, String role) {
        try {
            return userRoleMapper.countByUserIdAndRole(userId, role) > 0;
        } catch (Exception e) {
            logger.error("检查用户角色失败", e);
            return false;
        }
    }
    
    /**
     * 简单的Token生成（实际应用中应使用JWT）
     */
    private String generateToken(String userId) {
        return "user_" + userId + "_" + System.currentTimeMillis();
    }
    
    /**
     * 验证Token
     */
    public String validateToken(String token) {
        try {
            if (token == null || !token.startsWith("user_")) {
                return null;
            }
            
            String[] parts = token.split("_");
            if (parts.length < 3) {
                return null;
            }
            
            String userId = parts[1];
            
            // 验证用户是否存在且活跃
            User user = userMapper.findById(userId);
            if (user == null || !user.isActive()) {
                return null;
            }
            
            return userId;
            
        } catch (Exception e) {
            logger.error("验证Token失败", e);
            return null;
        }
    }
    
    /**
     * 获取用户列表（管理员功能）
     */
    public Map<String, Object> getUserList(int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<User> users = userMapper.findByPage(offset, size);
            int total = userMapper.count();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("users", users);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (total + size - 1) / size);
            
            return result;
            
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
    
    /**
     * 搜索用户
     */
    public List<User> searchUsers(String keyword) {
        try {
            return userMapper.searchUsers(keyword);
        } catch (Exception e) {
            logger.error("搜索用户失败", e);
            throw new RuntimeException("搜索用户失败: " + e.getMessage());
        }
    }
} 