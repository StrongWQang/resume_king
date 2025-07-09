-- 迁移脚本：更新简历表结构
-- 备份原表
CREATE TABLE resume_backup AS SELECT * FROM resume;

-- 添加新字段
ALTER TABLE resume 
ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间' AFTER create_time,
ADD COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-发布，2-归档，3-删除' AFTER update_time,
ADD COLUMN like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数量' AFTER status,
ADD COLUMN user_id VARCHAR(36) DEFAULT NULL COMMENT '用户ID，用于未来分片' AFTER like_count,
ADD COLUMN title VARCHAR(255) DEFAULT NULL COMMENT '简历标题' AFTER user_id,
ADD COLUMN is_template TINYINT(1) DEFAULT 0 COMMENT '是否为模板：0-否，1-是' AFTER title;

-- 更新现有数据
UPDATE resume SET 
  update_time = create_time,
  status = 1,
  like_count = 0,
  title = '我的简历';

-- 添加索引
ALTER TABLE resume 
ADD INDEX idx_status_create_time (status, create_time) COMMENT '状态和创建时间复合索引，用于列表查询',
ADD INDEX idx_like_count (like_count) COMMENT '点赞数索引，用于热门排序',
ADD INDEX idx_user_id (user_id) COMMENT '用户ID索引，用于未来按用户分片';

-- 添加表注释
ALTER TABLE resume COMMENT = '简历表';

-- 注意：以下命令在需要时取消注释执行
-- 添加分区表支持，为未来分库分表做准备
-- ALTER TABLE resume PARTITION BY HASH(user_id) PARTITIONS 4; 