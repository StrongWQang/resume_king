-- 创建临时表
CREATE TABLE resume_publish_request_temp (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `resume_id` bigint NOT NULL COMMENT '简历ID',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '简历标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '简历描述',
  `status` enum('PENDING','APPROVED','REJECTED','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '审批状态',
  `submit_time` datetime NOT NULL COMMENT '提交时间',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approver_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批人ID',
  `reject_reason` text COLLATE utf8mb4_unicode_ci COMMENT '拒绝理由',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`) COMMENT '简历ID索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_submit_time` (`submit_time`) COMMENT '提交时间索引',
  KEY `idx_approver_id` (`approver_id`) COMMENT '审批人ID索引',
  KEY `idx_resume_publish_request_status_submit_time` (`status`,`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历发布申请表';

-- 迁移数据（只迁移resume_id为数字格式的数据）
INSERT INTO resume_publish_request_temp
SELECT 
    id,
    CAST(resume_id AS UNSIGNED) as resume_id,
    user_id,
    title,
    description,
    status,
    submit_time,
    approve_time,
    approver_id,
    reject_reason,
    create_time,
    update_time
FROM resume_publish_request
WHERE resume_id REGEXP '^[0-9]+$';

-- 删除旧表
DROP TABLE resume_publish_request;

-- 重命名新表
RENAME TABLE resume_publish_request_temp TO resume_publish_request;

-- 添加外键约束
ALTER TABLE resume_publish_request
ADD CONSTRAINT `resume_publish_request_ibfk_1` 
FOREIGN KEY (`resume_id`) REFERENCES `resume` (`id`) ON DELETE CASCADE; 