package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.ResumePublishRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface ResumePublishRequestMapper {
    
    @Insert("INSERT INTO resume_publish_request (id, resume_id, user_id, title, description, status, " +
            "submit_time, approve_time, approver_id, reject_reason, create_time, update_time) " +
            "VALUES (#{id}, #{resumeId}, #{userId}, #{title}, #{description}, #{status}, " +
            "#{submitTime}, #{approveTime}, #{approverId}, #{rejectReason}, #{createTime}, #{updateTime})")
    void insert(ResumePublishRequest request);
    
    @Select("SELECT * FROM resume_publish_request WHERE id = #{id}")
    @Results(id = "resumePublishRequestResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "resumeId", column = "resume_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "approveTime", column = "approve_time"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "rejectReason", column = "reject_reason"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    ResumePublishRequest findById(Long id);
    
    @Select("SELECT * FROM resume_publish_request WHERE resume_id = #{resumeId}")
    @ResultMap("resumePublishRequestResult")
    List<ResumePublishRequest> findByResumeId(Long resumeId);
    
    @Select("SELECT * FROM resume_publish_request WHERE user_id = #{userId} ORDER BY submit_time DESC")
    @ResultMap("resumePublishRequestResult")
    List<ResumePublishRequest> findByUserId(String userId);
    
    @Select("SELECT * FROM resume_publish_request WHERE status = #{status} ORDER BY submit_time DESC")
    @ResultMap("resumePublishRequestResult")
    List<ResumePublishRequest> findByStatus(String status);
    
    @Select("SELECT * FROM resume_publish_request ORDER BY submit_time DESC")
    @ResultMap("resumePublishRequestResult")
    List<ResumePublishRequest> findAll();
    
    @Select("SELECT * FROM resume_publish_request ORDER BY submit_time DESC LIMIT #{limit} OFFSET #{offset}")
    @ResultMap("resumePublishRequestResult")
    List<ResumePublishRequest> findByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT COUNT(*) FROM resume_publish_request")
    int count();
    
    @Select("SELECT COUNT(*) FROM resume_publish_request WHERE status = #{status}")
    int countByStatus(String status);
    
    @Select("SELECT * FROM resume_publish_request WHERE status = 'PENDING' ORDER BY submit_time ASC")
    @ResultMap("resumePublishRequestResult")
    List<ResumePublishRequest> findPendingRequests();
    
    @Select("SELECT * FROM resume_publish_request WHERE approver_id = #{approverId} ORDER BY approve_time DESC")
    @ResultMap("resumePublishRequestResult")
    List<ResumePublishRequest> findByApproverId(String approverId);
    
    @Update("UPDATE resume_publish_request SET status = #{status}, approver_id = #{approverId}, " +
            "approve_time = #{approveTime}, reject_reason = #{rejectReason}, update_time = #{updateTime} " +
            "WHERE id = #{id}")
    void updateApprovalInfo(ResumePublishRequest request);
    
    @Update("UPDATE resume_publish_request SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updateTime") java.time.LocalDateTime updateTime);
    
    @Delete("DELETE FROM resume_publish_request WHERE id = #{id}")
    void deleteById(Long id);
    
    @Delete("DELETE FROM resume_publish_request WHERE resume_id = #{resumeId}")
    void deleteByResumeId(Long resumeId);
    
    // 批量审批
    @Update("UPDATE resume_publish_request SET status = #{status}, approver_id = #{approverId}, " +
            "approve_time = NOW(), update_time = NOW() WHERE id IN (${ids}) AND status = 'PENDING'")
    void batchUpdateStatus(@Param("ids") String ids, @Param("status") String status, @Param("approverId") String approverId);
    
    // 统计查询
    @Select("SELECT status, COUNT(*) as count FROM resume_publish_request GROUP BY status")
    @Results({
        @Result(property = "status", column = "status"),
        @Result(property = "count", column = "count")
    })
    List<Map<String, Object>> getStatusStatistics();
    
    // 获取审批工作台数据
    @Select("SELECT rpr.*, au.username as approver_name, r.like_count, r.create_time as resume_create_time " +
            "FROM resume_publish_request rpr " +
            "LEFT JOIN admin_user au ON rpr.approver_id = au.id " +
            "LEFT JOIN resume r ON rpr.resume_id = r.id " +
            "ORDER BY rpr.submit_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "resumeId", column = "resume_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "approveTime", column = "approve_time"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "rejectReason", column = "reject_reason"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "approverName", column = "approver_name"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "resumeCreateTime", column = "resume_create_time")
    })
    List<Map<String, Object>> getApprovalDashboard();
} 