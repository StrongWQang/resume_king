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
    
    // 业务方法：更新最后登录时间
    public void updateLastLogin() {
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：获取状态显示名称
    public String getStatusDisplayName() {
        return this.status == UserStatus.ACTIVE ? "活跃" : "禁用";
    }
    
    // 业务方法：验证用户权限
    public boolean canLogin() {
        return isActive();
    }
    
    // 业务方法：验证管理权限
    public boolean canManage() {
        return isActive();
    }
    
    // 业务方法：验证审批权限
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
    
    // 手动添加getter/setter方法以解决Lombok编译问题
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
} 