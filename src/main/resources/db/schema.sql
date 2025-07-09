CREATE DATABASE IF NOT EXISTS resume_king DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE resume_king;

CREATE TABLE IF NOT EXISTS resume (
    id VARCHAR(36) NOT NULL COMMENT '简历ID，使用雪花算法生成',
    content JSON NOT NULL COMMENT '简历内容JSON',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-发布，2-归档，3-删除',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数量',
    user_id VARCHAR(36) DEFAULT NULL COMMENT '用户ID，用于未来分片',
    title VARCHAR(255) DEFAULT NULL COMMENT '简历标题',
    is_template TINYINT(1) DEFAULT 0 COMMENT '是否为模板：0-否，1-是',
    PRIMARY KEY (id),
    INDEX idx_status_create_time (status, create_time) COMMENT '状态和创建时间复合索引，用于列表查询',
    INDEX idx_like_count (like_count) COMMENT '点赞数索引，用于热门排序',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引，用于未来按用户分片'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

-- 添加分区表支持，为未来分库分表做准备
-- 可以按照user_id进行分区，这里使用HASH分区示例
-- ALTER TABLE resume PARTITION BY HASH(user_id) PARTITIONS 4; 