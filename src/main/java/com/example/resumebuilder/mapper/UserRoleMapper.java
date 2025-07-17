package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.UserRole;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserRoleMapper {
    
    // 插入用户角色
    @Insert("INSERT INTO user_roles (id, user_id, role, create_time) VALUES " +
            "(#{id}, #{userId}, #{role}, #{createTime})")
    void insert(UserRole userRole);
    
    // 根据用户ID查找角色
    @Select("SELECT * FROM user_roles WHERE user_id = #{userId}")
    @Results(id = "userRoleResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "role", column = "role"),
        @Result(property = "createTime", column = "create_time")
    })
    List<UserRole> findByUserId(String userId);
    
    // 根据角色查找用户
    @Select("SELECT * FROM user_roles WHERE role = #{role}")
    @ResultMap("userRoleResult")
    List<UserRole> findByRole(String role);
    
    // 检查用户是否具有某个角色
    @Select("SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND role = #{role}")
    int countByUserIdAndRole(@Param("userId") String userId, @Param("role") String role);
    
    // 删除用户角色
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND role = #{role}")
    void deleteByUserIdAndRole(@Param("userId") String userId, @Param("role") String role);
    
    // 删除用户的所有角色
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    void deleteByUserId(String userId);
    
    // 获取用户的所有角色名称
    @Select("SELECT role FROM user_roles WHERE user_id = #{userId}")
    List<String> getRolesByUserId(String userId);
    
    // 统计某个角色的用户数量
    @Select("SELECT COUNT(*) FROM user_roles WHERE role = #{role}")
    int countByRole(String role);
    
    // 获取所有角色统计
    @Select("SELECT role, COUNT(*) as count FROM user_roles GROUP BY role")
    @Results({
        @Result(property = "role", column = "role"),
        @Result(property = "count", column = "count")
    })
    List<java.util.Map<String, Object>> getRoleStatistics();
} 