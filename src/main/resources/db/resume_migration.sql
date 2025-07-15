-- 创建新的临时表
CREATE TABLE `resume_new` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `content` json NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-草稿，1-发布，2-归档，3-删除',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数量',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID，用于未来分片',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简历标题',
  `is_template` tinyint(1) DEFAULT '0' COMMENT '是否为模板：0-否，1-是',
  `template_source` enum('SYSTEM','USER_PUBLISH') COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '模板来源：SYSTEM-系统内置，USER_PUBLISH-用户发布',
  `publish_request_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布申请ID',
  `like` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_create_time` (`status`,`create_time`) USING BTREE COMMENT '状态和创建时间复合索引，用于列表查询',
  KEY `idx_like_count` (`like_count`) USING BTREE COMMENT '点赞数索引，用于热门排序',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引，用于未来按用户分片',
  KEY `idx_template_source` (`template_source`) COMMENT '模板来源索引',
  KEY `idx_publish_request_id` (`publish_request_id`) COMMENT '发布申请ID索引',
  KEY `idx_resume_is_template_status` (`is_template`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='简历表';

-- 迁移雪花算法生成的ID数据（雪花ID为纯数字且长度为19位）
INSERT INTO resume_new 
SELECT 
    CAST(id AS UNSIGNED) as id,
    content,
    create_time,
    update_time,
    status,
    like_count,
    user_id,
    title,
    is_template,
    template_source,
    publish_request_id,
    `like`
FROM resume 
WHERE id REGEXP '^[0-9]{19}$';

-- 更新resume_publish_request表中的外键引用
UPDATE resume_publish_request rpr
SET resume_id = NULL
WHERE resume_id NOT IN (
    SELECT id FROM resume 
    WHERE id REGEXP '^[0-9]{19}$'
);

-- 删除旧表
DROP TABLE resume;

-- 重命名新表
RENAME TABLE resume_new TO resume;

-- 重新创建外键约束
ALTER TABLE resume_publish_request
ADD CONSTRAINT `resume_publish_request_ibfk_1` 
FOREIGN KEY (`resume_id`) REFERENCES `resume` (`id`) ON DELETE CASCADE;

-- 更新分片表结构
DROP TABLE IF EXISTS `resume_0`;
CREATE TABLE `resume_0` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `content` json NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-草稿，1-发布，2-归档，3-删除',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数量',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID，用于未来分片',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简历标题',
  `is_template` tinyint(1) DEFAULT '0' COMMENT '是否为模板：0-否，1-是',
  `template_source` enum('SYSTEM','USER_PUBLISH') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '模板来源：SYSTEM-系统内置，USER_PUBLISH-用户发布',
  `publish_request_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布申请ID',
  `like` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_create_time` (`status`,`create_time`) USING BTREE COMMENT '状态和创建时间复合索引，用于列表查询',
  KEY `idx_like_count` (`like_count`) USING BTREE COMMENT '点赞数索引，用于热门排序',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引，用于未来按用户分片',
  KEY `idx_template_source` (`template_source`) COMMENT '模板来源索引',
  KEY `idx_publish_request_id` (`publish_request_id`) COMMENT '发布申请ID索引',
  KEY `idx_resume_is_template_status` (`is_template`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='简历表';

DROP TABLE IF EXISTS `resume_1`;
CREATE TABLE `resume_1` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `content` json NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-草稿，1-发布，2-归档，3-删除',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数量',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID，用于未来分片',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简历标题',
  `is_template` tinyint(1) DEFAULT '0' COMMENT '是否为模板：0-否，1-是',
  `template_source` enum('SYSTEM','USER_PUBLISH') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '模板来源：SYSTEM-系统内置，USER_PUBLISH-用户发布',
  `publish_request_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布申请ID',
  `like` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_create_time` (`status`,`create_time`) USING BTREE COMMENT '状态和创建时间复合索引，用于列表查询',
  KEY `idx_like_count` (`like_count`) USING BTREE COMMENT '点赞数索引，用于热门排序',
  KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引，用于未来按用户分片',
  KEY `idx_template_source` (`template_source`) COMMENT '模板来源索引',
  KEY `idx_publish_request_id` (`publish_request_id`) COMMENT '发布申请ID索引',
  KEY `idx_resume_is_template_status` (`is_template`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='简历表'; 