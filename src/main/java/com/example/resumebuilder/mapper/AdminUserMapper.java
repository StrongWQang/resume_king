package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.AdminUser;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AdminUserMapper {
    
    @Insert("INSERT INTO admin_user (id, username, password, email, status, create_time, update_time) " +
            "VALUES (#{id}, #{username}, #{password}, #{email}, #{status}, #{createTime}, #{updateTime})")
    void insert(AdminUser adminUser);
    
    @Select("SELECT * FROM admin_user WHERE id = #{id}")
    @Results(id = "adminUserResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),
        @Result(property = "email", column = "email"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    AdminUser findById(String id);
    
    @Select("SELECT * FROM admin_user WHERE username = #{username}")
    @ResultMap("adminUserResult")
    AdminUser findByUsername(String username);
    
    @Select("SELECT * FROM admin_user WHERE email = #{email}")
    @ResultMap("adminUserResult")
    AdminUser findByEmail(String email);
    
    @Select("SELECT * FROM admin_user WHERE status = #{status} ORDER BY create_time DESC")
    @ResultMap("adminUserResult")
    List<AdminUser> findByStatus(String status);
    
    @Select("SELECT * FROM admin_user ORDER BY create_time DESC")
    @ResultMap("adminUserResult")
    List<AdminUser> findAll();
    
    @Select("SELECT * FROM admin_user ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    @ResultMap("adminUserResult")
    List<AdminUser> findByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT COUNT(*) FROM admin_user")
    int count();
    
    @Select("SELECT COUNT(*) FROM admin_user WHERE status = #{status}")
    int countByStatus(String status);
    
    @Select("SELECT * FROM admin_user WHERE status = 'ACTIVE' ORDER BY create_time DESC")
    @ResultMap("adminUserResult")
    List<AdminUser> findActiveUsers();
    
    @Update("UPDATE admin_user SET username = #{username}, email = #{email}, status = #{status}, " +
            "update_time = #{updateTime} WHERE id = #{id}")
    void update(AdminUser adminUser);
    
    @Update("UPDATE admin_user SET password = #{password}, update_time = #{updateTime} WHERE id = #{id}")
    void updatePassword(@Param("id") String id, @Param("password") String password, 
                       @Param("updateTime") java.time.LocalDateTime updateTime);
    
    @Update("UPDATE admin_user SET email = #{email}, update_time = #{updateTime} WHERE id = #{id}")
    void updateEmail(@Param("id") String id, @Param("email") String email, 
                    @Param("updateTime") java.time.LocalDateTime updateTime);
    
    @Update("UPDATE admin_user SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status, 
                     @Param("updateTime") java.time.LocalDateTime updateTime);
    
    @Delete("DELETE FROM admin_user WHERE id = #{id}")
    void deleteById(String id);
    
    // 验证用户登录
    @Select("SELECT * FROM admin_user WHERE username = #{username} AND status = 'ACTIVE'")
    @ResultMap("adminUserResult")
    AdminUser validateLogin(String username);
    
    // 获取安全的用户信息（不包含密码）
    @Select("SELECT id, username, email, status, create_time, update_time FROM admin_user WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    AdminUser getSafeUserInfo(String id);
    
    // 获取所有活跃用户的安全信息
    @Select("SELECT id, username, email, status, create_time, update_time FROM admin_user WHERE status = 'ACTIVE' ORDER BY create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    List<AdminUser> getAllActiveUsersSafe();
    
    // 检查用户名是否存在
    @Select("SELECT COUNT(*) FROM admin_user WHERE username = #{username}")
    int countByUsername(String username);
    
    // 检查邮箱是否存在
    @Select("SELECT COUNT(*) FROM admin_user WHERE email = #{email}")
    int countByEmail(String email);
    
    // 获取用户审批统计
    @Select("SELECT " +
            "au.id, " +
            "au.username, " +
            "au.email, " +
            "au.status, " +
            "COUNT(rpr.id) as approval_count, " +
            "SUM(CASE WHEN rpr.status = 'APPROVED' THEN 1 ELSE 0 END) as approved_count, " +
            "SUM(CASE WHEN rpr.status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count " +
            "FROM admin_user au " +
            "LEFT JOIN resume_publish_request rpr ON au.id = rpr.approver_id " +
            "WHERE au.status = 'ACTIVE' " +
            "GROUP BY au.id, au.username, au.email, au.status " +
            "ORDER BY approval_count DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "status", column = "status"),
        @Result(property = "approvalCount", column = "approval_count"),
        @Result(property = "approvedCount", column = "approved_count"),
        @Result(property = "rejectedCount", column = "rejected_count")
    })
    List<java.util.Map<String, Object>> getApprovalStatistics();
    
    // 按用户名或邮箱搜索
    @Select("SELECT id, username, email, status, create_time, update_time FROM admin_user " +
            "WHERE (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND status = 'ACTIVE' ORDER BY create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    List<AdminUser> searchUsers(String keyword);
} 