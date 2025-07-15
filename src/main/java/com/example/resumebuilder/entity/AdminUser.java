package com.example.resumebuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUser {
    private String id;
    private String username;
    @JsonIgnore  // 在JSON序列化时忽略密码字段
    private String password;
    private String email;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 用户状态枚举
    public enum UserStatus {
        ACTIVE,    // 活跃
        INACTIVE   // 非活跃
    }
    
    // 构造方法：创建新管理员
    public AdminUser(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = UserStatus.ACTIVE;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：激活用户
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：禁用用户
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：检查用户是否活跃
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
    
    // 业务方法：获取状态显示名称
    public String getStatusDisplayName() {
        switch (this.status) {
            case ACTIVE:
                return "活跃";
            case INACTIVE:
                return "禁用";
            default:
                return "未知";
        }
    }
    
    // 业务方法：更新密码
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：更新邮箱
    public void updateEmail(String newEmail) {
        this.email = newEmail;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：检查是否可以执行审批操作
    public boolean canApprove() {
        return isActive();
    }
    
    // 业务方法：获取管理员信息摘要
    public String getSummary() {
        return String.format("管理员[%s] - %s", username, getStatusDisplayName());
    }
    
    // 安全方法：获取脱敏信息（不包含密码）
    @JsonIgnore  // 在JSON序列化时忽略此方法
    public AdminUser getSafeInfo() {
        AdminUser safeUser = new AdminUser();
        safeUser.setId(this.id);
        safeUser.setUsername(this.username);
        safeUser.setEmail(this.email);
        safeUser.setStatus(this.status);
        safeUser.setCreateTime(this.createTime);
        safeUser.setUpdateTime(this.updateTime);
        return safeUser;
    }
} 