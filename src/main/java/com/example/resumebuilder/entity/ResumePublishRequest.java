package com.example.resumebuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumePublishRequest {
    private Long id;
    private Long resumeId;
    private String userId;
    private String title;
    private String description;
    private ApprovalStatus status;
    private LocalDateTime submitTime;
    private LocalDateTime approveTime;
    private String approverId;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 审批状态枚举
    public enum ApprovalStatus {
        PENDING,    // 待审批
        APPROVED,   // 已通过
        REJECTED,   // 已拒绝
        CANCELLED   // 已取消
    }
    
    // 构造方法：创建新申请
    public ResumePublishRequest(Long resumeId, String userId, String title, String description) {
        this.resumeId = resumeId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.status = ApprovalStatus.PENDING;
        this.submitTime = LocalDateTime.now();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：通过审批
    public void approve(String approverId) {
        this.status = ApprovalStatus.APPROVED;
        this.approverId = approverId;
        this.approveTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：拒绝审批
    public void reject(String approverId, String reason) {
        this.status = ApprovalStatus.REJECTED;
        this.approverId = approverId;
        this.rejectReason = reason;
        this.approveTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：取消申请
    public void cancel() {
        this.status = ApprovalStatus.CANCELLED;
        this.updateTime = LocalDateTime.now();
    }
    
    // 业务方法：检查是否可以审批
    public boolean canApprove() {
        return this.status == ApprovalStatus.PENDING;
    }
    
    // 业务方法：检查是否可以取消
    public boolean canCancel() {
        return this.status == ApprovalStatus.PENDING;
    }
    
    // 业务方法：获取状态显示名称
    public String getStatusDisplayName() {
        switch (this.status) {
            case PENDING:
                return "待审批";
            case APPROVED:
                return "已通过";
            case REJECTED:
                return "已拒绝";
            case CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }
    
    // 业务方法：检查是否已处理
    public boolean isProcessed() {
        return this.status == ApprovalStatus.APPROVED || 
               this.status == ApprovalStatus.REJECTED || 
               this.status == ApprovalStatus.CANCELLED;
    }
} 