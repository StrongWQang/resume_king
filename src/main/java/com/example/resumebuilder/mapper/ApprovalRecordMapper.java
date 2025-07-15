package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.ApprovalRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface ApprovalRecordMapper {
    
    @Insert("INSERT INTO approval_record (id, request_id, previous_status, current_status, " +
            "approver_id, operation, comment, create_time) " +
            "VALUES (#{id}, #{requestId}, #{previousStatus}, #{currentStatus}, " +
            "#{approverId}, #{operation}, #{comment}, #{createTime})")
    void insert(ApprovalRecord record);
    
    @Select("SELECT * FROM approval_record WHERE id = #{id}")
    @Results(id = "approvalRecordResult", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "requestId", column = "request_id"),
        @Result(property = "previousStatus", column = "previous_status"),
        @Result(property = "currentStatus", column = "current_status"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "createTime", column = "create_time")
    })
    ApprovalRecord findById(String id);
    
    @Select("SELECT * FROM approval_record WHERE request_id = #{requestId} ORDER BY create_time ASC")
    @ResultMap("approvalRecordResult")
    List<ApprovalRecord> findByRequestId(Long requestId);
    
    @Select("SELECT * FROM approval_record WHERE approver_id = #{approverId} ORDER BY create_time DESC")
    @ResultMap("approvalRecordResult")
    List<ApprovalRecord> findByApproverId(String approverId);
    
    @Select("SELECT * FROM approval_record WHERE operation = #{operation} ORDER BY create_time DESC")
    @ResultMap("approvalRecordResult")
    List<ApprovalRecord> findByOperation(String operation);
    
    @Select("SELECT * FROM approval_record ORDER BY create_time DESC")
    @ResultMap("approvalRecordResult")
    List<ApprovalRecord> findAll();
    
    @Select("SELECT * FROM approval_record ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    @ResultMap("approvalRecordResult")
    List<ApprovalRecord> findByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT COUNT(*) FROM approval_record")
    int count();
    
    @Select("SELECT COUNT(*) FROM approval_record WHERE request_id = #{requestId}")
    int countByRequestId(Long requestId);
    
    @Select("SELECT COUNT(*) FROM approval_record WHERE approver_id = #{approverId}")
    int countByApproverId(String approverId);
    
    @Delete("DELETE FROM approval_record WHERE id = #{id}")
    void deleteById(String id);
    
    @Delete("DELETE FROM approval_record WHERE request_id = #{requestId}")
    void deleteByRequestId(Long requestId);
    
    // 获取审批历史记录（包含审批人信息）
    @Select("SELECT ar.*, au.username as approver_name, au.email as approver_email " +
            "FROM approval_record ar " +
            "LEFT JOIN admin_user au ON ar.approver_id = au.id " +
            "WHERE ar.request_id = #{requestId} ORDER BY ar.create_time ASC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "requestId", column = "request_id"),
        @Result(property = "previousStatus", column = "previous_status"),
        @Result(property = "currentStatus", column = "current_status"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "approverName", column = "approver_name"),
        @Result(property = "approverEmail", column = "approver_email")
    })
    List<Map<String, Object>> getApprovalHistory(Long requestId);
    
    // 获取审批统计信息
    @Select("SELECT " +
            "operation, " +
            "COUNT(*) as count, " +
            "COUNT(DISTINCT approver_id) as approver_count " +
            "FROM approval_record " +
            "WHERE create_time >= #{startTime} AND create_time <= #{endTime} " +
            "GROUP BY operation")
    @Results({
        @Result(property = "operation", column = "operation"),
        @Result(property = "count", column = "count"),
        @Result(property = "approverCount", column = "approver_count")
    })
    List<Map<String, Object>> getOperationStatistics(
        @Param("startTime") java.time.LocalDateTime startTime, 
        @Param("endTime") java.time.LocalDateTime endTime
    );
    
    // 获取审批人工作量统计
    @Select("SELECT " +
            "ar.approver_id, " +
            "au.username as approver_name, " +
            "COUNT(*) as total_operations, " +
            "SUM(CASE WHEN ar.operation = 'APPROVE' THEN 1 ELSE 0 END) as approve_count, " +
            "SUM(CASE WHEN ar.operation = 'REJECT' THEN 1 ELSE 0 END) as reject_count " +
            "FROM approval_record ar " +
            "LEFT JOIN admin_user au ON ar.approver_id = au.id " +
            "WHERE ar.create_time >= #{startTime} AND ar.create_time <= #{endTime} " +
            "GROUP BY ar.approver_id, au.username " +
            "ORDER BY total_operations DESC")
    @Results({
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "approverName", column = "approver_name"),
        @Result(property = "totalOperations", column = "total_operations"),
        @Result(property = "approveCount", column = "approve_count"),
        @Result(property = "rejectCount", column = "reject_count")
    })
    List<Map<String, Object>> getApproverWorkload(
        @Param("startTime") java.time.LocalDateTime startTime, 
        @Param("endTime") java.time.LocalDateTime endTime
    );
    
    // 获取最近的审批记录
    @Select("SELECT ar.*, au.username as approver_name, rpr.title as request_title " +
            "FROM approval_record ar " +
            "LEFT JOIN admin_user au ON ar.approver_id = au.id " +
            "LEFT JOIN resume_publish_request rpr ON ar.request_id = rpr.id " +
            "ORDER BY ar.create_time DESC " +
            "LIMIT #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "requestId", column = "request_id"),
        @Result(property = "previousStatus", column = "previous_status"),
        @Result(property = "currentStatus", column = "current_status"),
        @Result(property = "approverId", column = "approver_id"),
        @Result(property = "operation", column = "operation"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "approverName", column = "approver_name"),
        @Result(property = "requestTitle", column = "request_title")
    })
    List<Map<String, Object>> getRecentApprovalRecords(int limit);
} 