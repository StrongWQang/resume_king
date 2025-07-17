-- 用户表扩展
-- 删除现有的users表并创建新的用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `status` enum('ACTIVE','INACTIVE','SUSPENDED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-未激活，SUSPENDED-已暂停',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `login_count` int DEFAULT '0' COMMENT '登录次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`) COMMENT '用户名索引',
  KEY `idx_email` (`email`) COMMENT '邮箱索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 删除现有的roles表并创建新的用户角色表
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `user_roles` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `role` enum('USER','VIP','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'USER' COMMENT '用户角色：USER-普通用户，VIP-VIP用户，ADMIN-管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_role` (`role`) COMMENT '角色索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色表';

-- 删除permissions表（保留给管理员系统使用）
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'role',
  `resource` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'resource',
  `action` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'action',
  UNIQUE KEY `uk_role_permission` (`role`,`resource`,`action`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化一些测试用户数据
INSERT INTO `users` (`id`, `username`, `password`, `email`, `nickname`, `status`, `create_time`, `update_time`) VALUES 
('user-001', 'test_user', '$2a$10$K2aKEypirfR1/xvKpRmu9OaqOurDtGZ9xsCpaoCuVZz8y6KDte0cu', 'test@example.com', '测试用户', 'ACTIVE', NOW(), NOW()),
('user-002', 'demo_user', '$2a$10$RVsT5TpdApIQMGZqN7t2.u6pMZ1iOxC0QS6rfHNunN112Kxhjhhi.', 'demo@example.com', '演示用户', 'ACTIVE', NOW(), NOW());

-- 为测试用户分配角色
INSERT INTO `user_roles` (`id`, `user_id`, `role`, `create_time`) VALUES 
('role-001', 'user-001', 'USER', NOW()),
('role-002', 'user-002', 'USER', NOW()); 