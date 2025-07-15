package com.example.resumebuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRecord {
    private String id;
    private Long requestId;
    private ResumePublishRequest.ApprovalStatus previousStatus;
    private ResumePublishRequest.ApprovalStatus currentStatus;
    private String approverId;
    private OperationType operation;
    private String comment;
    private LocalDateTime createTime;
    
    // 操作类型枚举
    public enum OperationType {
        SUBMIT,    // 提交申请
        APPROVE,   // 审批通过
        REJECT,    // 审批拒绝
        CANCEL     // 取消申请
    }
    
    // 构造方法：创建新记录
    public ApprovalRecord(String requestId, 
                         ResumePublishRequest.ApprovalStatus previousStatus,
                         ResumePublishRequest.ApprovalStatus currentStatus,
                         String approverId,
                         OperationType operation,
                         String comment) {
        this.requestId = Long.parseLong(requestId); // Assuming requestId is a String in the constructor
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.approverId = approverId;
        this.operation = operation;
        this.comment = comment;
        this.createTime = LocalDateTime.now();
    }
    
    // 业务方法：获取操作显示名称
    public String getOperationDisplayName() {
        switch (this.operation) {
            case SUBMIT:
                return "提交申请";
            case APPROVE:
                return "审批通过";
            case REJECT:
                return "审批拒绝";
            case CANCEL:
                return "取消申请";
            default:
                return "未知操作";
        }
    }
    
    // 业务方法：获取状态变化描述
    public String getStatusChangeDescription() {
        String prevStatus = previousStatus != null ? getStatusDisplayName(previousStatus) : "无";
        String currStatus = getStatusDisplayName(currentStatus);
        return String.format("从 %s 变更为 %s", prevStatus, currStatus);
    }
    
    // 辅助方法：获取状态显示名称
    private String getStatusDisplayName(ResumePublishRequest.ApprovalStatus status) {
        switch (status) {
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
    
    // 业务方法：检查是否为最终状态
    public boolean isFinalOperation() {
        return this.operation == OperationType.APPROVE || 
               this.operation == OperationType.REJECT || 
               this.operation == OperationType.CANCEL;
    }
    
    // 业务方法：获取操作结果
    public String getOperationResult() {
        switch (this.operation) {
            case SUBMIT:
                return "申请已提交，等待审批";
            case APPROVE:
                return "申请已通过";
            case REJECT:
                return "申请已拒绝：" + (comment != null ? comment : "无理由");
            case CANCEL:
                return "申请已取消";
            default:
                return "未知结果";
        }
    }
} 