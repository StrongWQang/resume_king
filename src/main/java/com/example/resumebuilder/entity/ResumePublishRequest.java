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
    
    // 手动添加getter/setter方法以解决Lombok编译问题
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getResumeId() {
        return resumeId;
    }
    
    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ApprovalStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getSubmitTime() {
        return submitTime;
    }
    
    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }
    
    public LocalDateTime getApproveTime() {
        return approveTime;
    }
    
    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }
    
    public String getApproverId() {
        return approverId;
    }
    
    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }
    
    public String getRejectReason() {
        return rejectReason;
    }
    
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
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