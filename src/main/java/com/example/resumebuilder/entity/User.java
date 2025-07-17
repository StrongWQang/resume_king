package com.example.resumebuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String username;
    @JsonIgnore  // 在JSON序列化时忽略密码字段
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    private String phone;
    private UserStatus status;
    private LocalDateTime lastLoginTime;
    private Integer loginCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 用户状态枚举
    public enum UserStatus {
        ACTIVE,      // 活跃
        INACTIVE,    // 未激活
        SUSPENDED    // 已暂停
    }
    
    // 构造方法：创建新用户
    public User(String username, String password, String email, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.status = UserStatus.ACTIVE;
        this.loginCount = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：激活用户
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：停用用户
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：暂停用户
    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：检查用户是否活跃
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
    
    // 业务方法：更新最后登录时间
    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
        this.loginCount = this.loginCount == null ? 1 : this.loginCount + 1;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：获取状态显示名称
    public String getStatusDisplayName() {
        switch (this.status) {
            case ACTIVE:
                return "活跃";
            case INACTIVE:
                return "未激活";
            case SUSPENDED:
                return "已暂停";
            default:
                return "未知";
        }
    }
    
    // 业务方法：更新用户信息
    public void updateProfile(String nickname, String email, String phone, String avatar) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (email != null) {
            this.email = email;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：更新密码
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：获取安全的用户信息（不包含密码）
    public Map<String, Object> getSafeInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("id", this.id);
        info.put("username", this.username);
        info.put("email", this.email);
        info.put("nickname", this.nickname);
        info.put("avatar", this.avatar);
        info.put("phone", this.phone);
        info.put("status", this.status);
        info.put("statusDisplayName", this.getStatusDisplayName());
        info.put("lastLoginTime", this.lastLoginTime);
        info.put("loginCount", this.loginCount);
        info.put("createTime", this.createTime);
        info.put("updateTime", this.updateTime);
        return info;
    }
    
    // 业务方法：验证用户是否可以登录
    public boolean canLogin() {
        return this.status == UserStatus.ACTIVE;
    }
    
    // 业务方法：获取显示名称（优先使用昵称，否则使用用户名）
    public String getDisplayName() {
        return this.nickname != null && !this.nickname.isEmpty() ? this.nickname : this.username;
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
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public Integer getLoginCount() {
        return loginCount;
    }
    
    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
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