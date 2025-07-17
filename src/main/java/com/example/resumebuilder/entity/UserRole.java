package com.example.resumebuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
    private String id;
    private String userId;
    private Role role;
    private LocalDateTime createTime;
    
    // 用户角色枚举
    public enum Role {
        USER,    // 普通用户
        VIP,     // VIP用户
        ADMIN    // 管理员
    }
    
    // 构造方法：创建新角色
    public UserRole(String userId, Role role) {
        this.userId = userId;
        this.role = role;
        this.createTime = LocalDateTime.now();
    }
    
    // 业务方法：获取角色显示名称
    public String getRoleDisplayName() {
        switch (this.role) {
            case USER:
                return "普通用户";
            case VIP:
                return "VIP用户";
            case ADMIN:
                return "管理员";
            default:
                return "未知";
        }
    }
    
    // 业务方法：检查是否为管理员
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
    
    // 业务方法：检查是否为VIP用户
    public boolean isVip() {
        return this.role == Role.VIP;
    }
    
    // 业务方法：检查是否为普通用户
    public boolean isUser() {
        return this.role == Role.USER;
    }
    
    // 手动添加getter/setter方法以解决Lombok编译问题
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
} 