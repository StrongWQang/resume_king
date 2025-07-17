package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.User;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    
    // 插入新用户
    @Insert("INSERT INTO users (id, username, password, email, nickname, avatar, phone, status, " +
            "last_login_time, login_count, create_time, update_time) VALUES " +
            "(#{id}, #{username}, #{password}, #{email}, #{nickname}, #{avatar}, #{phone}, #{status}, " +
            "#{lastLoginTime}, #{loginCount}, #{createTime}, #{updateTime})")
    void insert(User user);
    
    // 根据ID查找用户
    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results(id = "userResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),
        @Result(property = "email", column = "email"),
        @Result(property = "nickname", column = "nickname"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "status", column = "status"),
        @Result(property = "lastLoginTime", column = "last_login_time"),
        @Result(property = "loginCount", column = "login_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    User findById(String id);
    
    // 根据用户名查找用户
    @Select("SELECT * FROM users WHERE username = #{username}")
    @ResultMap("userResult")
    User findByUsername(String username);
    
    // 根据邮箱查找用户
    @Select("SELECT * FROM users WHERE email = #{email}")
    @ResultMap("userResult")
    User findByEmail(String email);
    
    // 验证用户登录
    @Select("SELECT * FROM users WHERE username = #{username} AND status = 'ACTIVE'")
    @ResultMap("userResult")
    User validateLogin(String username);
    
    // 根据邮箱验证用户登录
    @Select("SELECT * FROM users WHERE email = #{email} AND status = 'ACTIVE'")
    @ResultMap("userResult")
    User validateLoginByEmail(String email);
    
    // 获取安全的用户信息（不包含密码）
    @Select("SELECT id, username, email, nickname, avatar, phone, status, last_login_time, " +
            "login_count, create_time, update_time FROM users WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "nickname", column = "nickname"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "status", column = "status"),
        @Result(property = "lastLoginTime", column = "last_login_time"),
        @Result(property = "loginCount", column = "login_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    User getSafeUserInfo(String id);
    
    // 更新用户信息
    @Update("UPDATE users SET username = #{username}, email = #{email}, nickname = #{nickname}, " +
            "avatar = #{avatar}, phone = #{phone}, status = #{status}, update_time = #{updateTime} " +
            "WHERE id = #{id}")
    void update(User user);
    
    // 更新密码
    @Update("UPDATE users SET password = #{password}, update_time = #{updateTime} WHERE id = #{id}")
    void updatePassword(@Param("id") String id, @Param("password") String password, 
                       @Param("updateTime") LocalDateTime updateTime);
    
    // 更新最后登录时间
    @Update("UPDATE users SET last_login_time = #{lastLoginTime}, login_count = #{loginCount}, " +
            "update_time = #{updateTime} WHERE id = #{id}")
    void updateLastLoginTime(@Param("id") String id, @Param("lastLoginTime") LocalDateTime lastLoginTime, 
                            @Param("loginCount") Integer loginCount, @Param("updateTime") LocalDateTime updateTime);
    
    // 更新用户状态
    @Update("UPDATE users SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") String status, 
                     @Param("updateTime") LocalDateTime updateTime);
    
    // 根据状态查找用户
    @Select("SELECT * FROM users WHERE status = #{status} ORDER BY create_time DESC")
    @ResultMap("userResult")
    List<User> findByStatus(String status);
    
    // 获取所有用户
    @Select("SELECT * FROM users ORDER BY create_time DESC")
    @ResultMap("userResult")
    List<User> findAll();
    
    // 分页查询用户
    @Select("SELECT * FROM users ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    @ResultMap("userResult")
    List<User> findByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    // 搜索用户
    @Select("SELECT * FROM users WHERE username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY create_time DESC")
    @ResultMap("userResult")
    List<User> searchUsers(String keyword);
    
    // 统计用户数量
    @Select("SELECT COUNT(*) FROM users")
    int count();
    
    // 根据状态统计用户数量
    @Select("SELECT COUNT(*) FROM users WHERE status = #{status}")
    int countByStatus(String status);
    
    // 检查用户名是否存在
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(String username);
    
    // 检查邮箱是否存在
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(String email);
    
    // 删除用户
    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(String id);
    
    // 获取活跃用户列表
    @Select("SELECT * FROM users WHERE status = 'ACTIVE' ORDER BY create_time DESC")
    @ResultMap("userResult")
    List<User> findActiveUsers();
    
    // 获取用户的简历统计
    @Select("SELECT COUNT(*) FROM resume WHERE user_id = #{userId} AND status != 3")
    List<Integer> getUserResumeCount(String userId);
} 