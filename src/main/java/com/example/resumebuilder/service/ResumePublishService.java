package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.ApprovalRecord;
import com.example.resumebuilder.entity.Resume;
import com.example.resumebuilder.entity.ResumePublishRequest;
import com.example.resumebuilder.mapper.ApprovalRecordMapper;
import com.example.resumebuilder.mapper.ResumeMapper;
import com.example.resumebuilder.mapper.ResumePublishRequestMapper;
import com.example.resumebuilder.util.SnowflakeIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumePublishService {
    private static final Logger logger = LoggerFactory.getLogger(ResumePublishService.class);
    
    @Autowired
    private ResumePublishRequestMapper requestMapper;
    
    @Autowired
    private ApprovalRecordMapper recordMapper;
    
    @Autowired
    private ResumeMapper resumeMapper;
    
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 提交简历发布申请
     * 状态机：NULL -> PENDING
     */
    @Transactional
    public String submitPublishRequest(Long resumeId, String userId, String title, String description) {
        try {
            logger.info("开始提交简历发布申请, resumeId: {}, userId: {}", resumeId, userId);
            
            // 1. 验证简历是否存在
            Resume resume = resumeMapper.findById(resumeId);
            if (resume == null) {
                throw new RuntimeException("简历不存在");
            }
            
            // 2. 检查是否已经提交过申请
            List<ResumePublishRequest> existingRequests = requestMapper.findByResumeId(resumeId);
            for (ResumePublishRequest request : existingRequests) {
                if (request.getStatus() == ResumePublishRequest.ApprovalStatus.PENDING) {
                    throw new RuntimeException("该简历已有待审批的发布申请");
                }
            }
            
            // 3. 创建发布申请
            String requestId = String.valueOf(idGenerator.nextId());
            ResumePublishRequest request = new ResumePublishRequest(resumeId, userId, title, description);
            request.setId(Long.valueOf(requestId));
            
            // 4. 保存申请记录
            requestMapper.insert(request);
            
            // 5. 记录状态变更历史
            ApprovalRecord record = new ApprovalRecord(
                requestId,
                null,
                ResumePublishRequest.ApprovalStatus.PENDING,
                null,
                ApprovalRecord.OperationType.SUBMIT,
                "用户提交简历发布申请"
            );
            record.setId(String.valueOf(idGenerator.nextId()));
            recordMapper.insert(record);
            
            logger.info("简历发布申请提交成功, requestId: {}", requestId);
            return requestId;
            
        } catch (Exception e) {
            logger.error("提交简历发布申请失败", e);
            throw new RuntimeException("提交申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批通过申请
     * 状态机：PENDING -> APPROVED
     */
    @Transactional
    public void approveRequest(String requestId, String approverId, String comment) {
        try {
            logger.info("开始审批通过申请, requestId: {}, approverId: {}", requestId, approverId);
            
            // 1. 获取申请记录
            ResumePublishRequest request = requestMapper.findById(Long.valueOf(requestId));
            if (request == null) {
                throw new RuntimeException("申请记录不存在");
            }
            
            // 2. 检查状态机：只能从PENDING状态变更为APPROVED
            if (!request.canApprove()) {
                throw new RuntimeException("申请状态不允许审批通过，当前状态：" + request.getStatus());
            }
            
            // 3. 更新申请状态
            request.approve(approverId);
            requestMapper.updateApprovalInfo(request);
            
            // 4. 更新简历为模板状态
            Resume resume = resumeMapper.findById(request.getResumeId());
            if (resume != null) {
                resume.setIsTemplate(true);
                resume.setStatus(Resume.STATUS_PUBLISHED);
                resume.setUpdateTime(LocalDateTime.now());
                resumeMapper.updateResume(resume);
            }
            
            // 5. 记录审批历史
            ApprovalRecord record = new ApprovalRecord(
                requestId,
                ResumePublishRequest.ApprovalStatus.PENDING,
                ResumePublishRequest.ApprovalStatus.APPROVED,
                approverId,
                ApprovalRecord.OperationType.APPROVE,
                comment != null ? comment : "审批通过"
            );
            record.setId(String.valueOf(idGenerator.nextId()));
            recordMapper.insert(record);
            
            logger.info("申请审批通过成功, requestId: {}", requestId);
            
        } catch (Exception e) {
            logger.error("审批通过申请失败", e);
            throw new RuntimeException("审批通过失败: " + e.getMessage());
        }
    }
    
    /**
     * 拒绝审批申请
     * 状态机：PENDING -> REJECTED
     */
    @Transactional
    public void rejectRequest(String requestId, String approverId, String reason) {
        try {
            logger.info("开始拒绝申请, requestId: {}, approverId: {}", requestId, approverId);
            
            // 1. 获取申请记录
            ResumePublishRequest request = requestMapper.findById(Long.valueOf(requestId));
            if (request == null) {
                throw new RuntimeException("申请记录不存在");
            }
            
            // 2. 检查状态机：只能从PENDING状态变更为REJECTED
            if (!request.canApprove()) {
                throw new RuntimeException("申请状态不允许拒绝，当前状态：" + request.getStatus());
            }
            
            // 3. 更新申请状态
            request.reject(approverId, reason);
            requestMapper.updateApprovalInfo(request);
            
            // 4. 记录审批历史
            ApprovalRecord record = new ApprovalRecord(
                requestId,
                ResumePublishRequest.ApprovalStatus.PENDING,
                ResumePublishRequest.ApprovalStatus.REJECTED,
                approverId,
                ApprovalRecord.OperationType.REJECT,
                reason
            );
            record.setId(String.valueOf(idGenerator.nextId()));
            recordMapper.insert(record);
            
            logger.info("申请拒绝成功, requestId: {}", requestId);
            
        } catch (Exception e) {
            logger.error("拒绝申请失败", e);
            throw new RuntimeException("拒绝申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消申请
     * 状态机：PENDING -> CANCELLED
     */
    @Transactional
    public void cancelRequest(String requestId, String userId) {
        try {
            logger.info("开始取消申请, requestId: {}, userId: {}", requestId, userId);
            
            // 1. 获取申请记录
            ResumePublishRequest request = requestMapper.findById(Long.valueOf(requestId));
            if (request == null) {
                throw new RuntimeException("申请记录不存在");
            }
            
            // 2. 检查权限（如果有userId，需要验证是否为申请人）
            if (userId != null && !userId.equals(request.getUserId())) {
                throw new RuntimeException("无权限取消该申请");
            }
            
            // 3. 检查状态机：只能从PENDING状态变更为CANCELLED
            if (!request.canCancel()) {
                throw new RuntimeException("申请状态不允许取消，当前状态：" + request.getStatus());
            }
            
            // 4. 更新申请状态
            request.cancel();
            requestMapper.updateApprovalInfo(request);
            
            // 5. 记录操作历史
            ApprovalRecord record = new ApprovalRecord(
                requestId,
                ResumePublishRequest.ApprovalStatus.PENDING,
                ResumePublishRequest.ApprovalStatus.CANCELLED,
                userId,
                ApprovalRecord.OperationType.CANCEL,
                "用户取消申请"
            );
            record.setId(String.valueOf(idGenerator.nextId()));
            recordMapper.insert(record);
            
            logger.info("申请取消成功, requestId: {}", requestId);
            
        } catch (Exception e) {
            logger.error("取消申请失败", e);
            throw new RuntimeException("取消申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取申请详情
     */
    public ResumePublishRequest getRequestDetail(String requestId) {
        try {
            ResumePublishRequest request = requestMapper.findById(Long.valueOf(requestId));
            if (request == null) {
                throw new RuntimeException("申请记录不存在");
            }
            return request;
        } catch (Exception e) {
            logger.error("获取申请详情失败", e);
            throw new RuntimeException("获取申请详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取申请列表（分页）
     */
    public Map<String, Object> getRequestList(int page, int size, String status) {
        try {
            int offset = (page - 1) * size;
            List<ResumePublishRequest> requests;
            int total;
            
            if (status != null && !status.isEmpty()) {
                requests = requestMapper.findByStatus(status);
                total = requestMapper.countByStatus(status);
            } else {
                requests = requestMapper.findByPage(offset, size);
                total = requestMapper.count();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("requests", requests);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (total + size - 1) / size);
            
            return result;
        } catch (Exception e) {
            logger.error("获取申请列表失败", e);
            throw new RuntimeException("获取申请列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待审批申请列表
     */
    public List<ResumePublishRequest> getPendingRequests() {
        try {
            return requestMapper.findPendingRequests();
        } catch (Exception e) {
            logger.error("获取待审批申请列表失败", e);
            throw new RuntimeException("获取待审批申请列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的申请记录
     */
    public List<ResumePublishRequest> getUserRequests(String userId) {
        try {
            return requestMapper.findByUserId(userId);
        } catch (Exception e) {
            logger.error("获取用户申请记录失败", e);
            throw new RuntimeException("获取用户申请记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取申请状态统计
     */
    public Map<String, Object> getRequestStatistics() {
        try {
            List<Map<String, Object>> statistics = requestMapper.getStatusStatistics();
            Map<String, Object> result = new HashMap<>();
            
            // 初始化所有状态的计数为0
            for (ResumePublishRequest.ApprovalStatus status : ResumePublishRequest.ApprovalStatus.values()) {
                result.put(status.name(), 0);
            }
            
            // 更新实际的计数
            for (Map<String, Object> stat : statistics) {
                String status = (String) stat.get("status");
                Long count = (Long) stat.get("count");
                result.put(status, count);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("获取申请状态统计失败", e);
            throw new RuntimeException("获取申请状态统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取审批工作台数据
     */
    public List<Map<String, Object>> getApprovalDashboard() {
        try {
            return requestMapper.getApprovalDashboard();
        } catch (Exception e) {
            logger.error("获取审批工作台数据失败", e);
            throw new RuntimeException("获取审批工作台数据失败: " + e.getMessage());
        }
    }

    /**
     * 批量审批
     */
    @Transactional
    public void batchApprove(List<String> requestIds, String approverId, String action, String comment) {
        try {
            logger.info("开始批量审批, requestIds: {}, approverId: {}, action: {}", requestIds, approverId, action);
            
            for (String requestId : requestIds) {
                try {
                    if ("approve".equals(action)) {
                        approveRequest(requestId, approverId, comment);
                    } else if ("reject".equals(action)) {
                        rejectRequest(requestId, approverId, comment);
                    }
                } catch (Exception e) {
                    logger.error("处理申请 {} 失败: {}", requestId, e.getMessage());
                    throw new RuntimeException("处理申请 " + requestId + " 失败: " + e.getMessage());
                }
            }
            
            logger.info("批量审批完成");
        } catch (Exception e) {
            logger.error("批量审批失败", e);
            throw new RuntimeException("批量审批失败: " + e.getMessage());
        }
    }
} 