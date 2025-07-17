/*
 Navicat Premium Dump SQL

 Source Server         : resume_king1
 Source Server Type    : MySQL
 Source Server Version : 80020 (8.0.20)
 Source Host           : 47.108.255.49:3306
 Source Schema         : resume_king

 Target Server Type    : MySQL
 Target Server Version : 80020 (8.0.20)
 File Encoding         : 65001

 Date: 16/07/2025 21:47:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`) COMMENT '用户名索引',
  KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ----------------------------
-- Records of admin_user
-- ----------------------------
BEGIN;
INSERT INTO `admin_user` (`id`, `username`, `password`, `email`, `status`, `create_time`, `update_time`) VALUES ('admin-001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqmQUzJ/8EfIrSIjEqKuJKPTG', 'admin@resumeking.com', 'ACTIVE', '2025-07-14 11:31:23', '2025-07-14 11:31:23');
COMMIT;

-- ----------------------------
-- Table structure for approval_record
-- ----------------------------
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `request_id` bigint NOT NULL COMMENT '申请ID',
  `previous_status` enum('PENDING','APPROVED','REJECTED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前一个状态',
  `current_status` enum('PENDING','APPROVED','REJECTED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '当前状态',
  `approver_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批人ID',
  `operation` enum('SUBMIT','APPROVE','REJECT','CANCEL') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_request_id` (`request_id`) COMMENT '申请ID索引',
  KEY `idx_approver_id` (`approver_id`) COMMENT '审批人ID索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  KEY `idx_approval_record_request_create_time` (`request_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批记录表';

-- ----------------------------
-- Records of approval_record
-- ----------------------------
BEGIN;
INSERT INTO `approval_record` (`id`, `request_id`, `previous_status`, `current_status`, `approver_id`, `operation`, `comment`, `create_time`) VALUES ('600285232536742574', 600285232008260607, NULL, 'PENDING', NULL, 'SUBMIT', '用户提交简历发布申请', '2025-07-15 19:19:12');
INSERT INTO `approval_record` (`id`, `request_id`, `previous_status`, `current_status`, `approver_id`, `operation`, `comment`, `create_time`) VALUES ('600285361025050164', 600285232008260607, 'PENDING', 'APPROVED', 'admin-001', 'APPROVE', '可以的', '2025-07-15 19:19:43');
INSERT INTO `approval_record` (`id`, `request_id`, `previous_status`, `current_status`, `approver_id`, `operation`, `comment`, `create_time`) VALUES ('600288871636526440', 600288871133210025, NULL, 'PENDING', NULL, 'SUBMIT', '用户提交简历发布申请', '2025-07-15 19:33:40');
INSERT INTO `approval_record` (`id`, `request_id`, `previous_status`, `current_status`, `approver_id`, `operation`, `comment`, `create_time`) VALUES ('600288914800108060', 600288871133210025, 'PENDING', 'APPROVED', 'admin-001', 'APPROVE', '999999', '2025-07-15 19:33:50');
COMMIT;

-- ----------------------------
-- Table structure for company_logo
-- ----------------------------
DROP TABLE IF EXISTS `company_logo`;
CREATE TABLE `company_logo` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司名称',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'logo图片URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='公司logo表';

-- ----------------------------
-- Records of company_logo
-- ----------------------------
BEGIN;
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('1', '小红书', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/%E5%B0%8F%E7%BA%A2%E4%B9%A6-seeklogo.png', '2025-06-15 15:31:26', '2025-06-15 17:53:04');
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('2', '美团', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/%E7%BE%8E%E5%9B%A2_46fb2ec6-10dd-4799-9846-7a3e33c54675.png', '2025-06-15 17:42:44', '2025-06-15 17:42:47');
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('3', '百度', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/%E7%99%BE%E5%BA%A6_e53b9184-62f8-45bb-a65b-bcc753fbc87c.png', '2025-06-15 17:43:03', '2025-06-15 17:43:05');
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('4', 'bilibili', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/bilibili-seeklogo.png', '2025-06-15 17:52:24', '2025-06-15 17:53:12');
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('5', '拼多多', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/%E6%8B%BC%E5%A4%9A%E5%A4%9A_1749980841948.png', '2025-06-15 17:52:48', '2025-06-15 17:52:50');
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('6', '字节跳动', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/bytedance-seeklogo.png', '2025-06-15 23:13:02', '2025-06-15 23:13:04');
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('7', '阿里巴巴', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/Alibabadotcom--Streamline-Simple-Icons.svg', '2025-06-15 23:13:20', '2025-06-15 23:13:21');
INSERT INTO `company_logo` (`id`, `name`, `url`, `create_time`, `update_time`) VALUES ('8', '阿里云', 'https://web-oss-learn.oss-cn-beijing.aliyuncs.com/Alibabacloud--Streamline-Simple-Icons.svg', '2025-06-15 23:13:49', '2025-06-15 23:13:51');
COMMIT;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'group_id',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'configuration description',
  `c_use` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'configuration usage',
  `effect` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '配置生效的描述',
  `type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '配置的类型',
  `c_schema` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT '配置的模式',
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '密钥',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info';

-- ----------------------------
-- Records of config_info
-- ----------------------------
BEGIN;
INSERT INTO `config_info` (`id`, `data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`) VALUES (1, 'resume_king', 'DEFAULT_GROUP', 'spring:\n  servlet:\n    multipart:\n      max-file-size: 10MB\n      max-request-size: 10MB\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://47.108.255.49:3321/sharding_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    #url: jdbc:mysql://localhost:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    username: root\n    password: root\nmybatis:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.example.resumebuilder.entity\n  configuration:\n    map-underscore-to-camel-case: true\n\naliyun:\n  oss:\n    endpoint: oss-cn-beijing.aliyuncs.com\n    accessKeyId: LTAI5t7Vr2TX5KzUtorBufb4\n    accessKeySecret: cic32QP7wzZm8UdqzKngFod00h9BED\n    bucketName: web-oss-learn\n    urlPrefix: https://web-oss-learn.oss-cn-beijing.aliyuncs.com/\n\ndeepseek:\n  api:\n    key: sk-92d8e843b30149969cb761e6f595f412\n    url: https://api.deepseek.com/v1/chat/completions\n\n# 雪花算法配置\nsnowflake:\n  datacenter-id: 1  # 数据中心ID，可根据实际部署环境调整\n  machine-id: 0     # 机器ID，设置为0将自动根据主机名生成\n  clock-backward-wait-time: 5000  # 时钟回拨最大等待时间(毫秒)\n  max-clock-backward-time: 1000   # 最大允许时钟回拨时间(毫秒)，超过此值使用借号方案    ', 'a281fb5aa468d5f30d1b68f15901671a', '2025-07-09 22:22:01', '2025-07-14 19:24:30', NULL, '223.104.40.92', '', '', '简历王配置', '', '', 'yaml', '', '');
INSERT INTO `config_info` (`id`, `data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`) VALUES (2, 'nacos.cfg.dataIdfoo', 'foo', 'helloWorld', '1a833da63a6b7e20098dae06d06602e1', '2025-07-12 02:19:09', '2025-07-14 16:22:42', NULL, '194.102.39.128', '', '', NULL, NULL, NULL, 'text', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'datum_id',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='增加租户字段';

-- ----------------------------
-- Records of config_info_aggr
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '密钥',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_beta';

-- ----------------------------
-- Records of config_info_beta
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_info_tag';

-- ----------------------------
-- Records of config_info_tag
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation` (
  `id` bigint NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增长标识',
  PRIMARY KEY (`nid`),
  UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='config_tag_relation';

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='集群、各Group容量信息表';

-- ----------------------------
-- Records of group_capacity
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info` (
  `id` bigint unsigned NOT NULL COMMENT 'id',
  `nid` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增标识',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'source ip',
  `op_type` char(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'operation type',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '密钥',
  PRIMARY KEY (`nid`),
  KEY `idx_gmt_create` (`gmt_create`),
  KEY `idx_gmt_modified` (`gmt_modified`),
  KEY `idx_did` (`data_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='多租户改造';

-- ----------------------------
-- Records of his_config_info
-- ----------------------------
BEGIN;
INSERT INTO `his_config_info` (`id`, `nid`, `data_id`, `group_id`, `app_name`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `op_type`, `tenant_id`, `encrypted_data_key`) VALUES (0, 1, 'resume_king', 'DEFAULT_GROUP', '', 'spring:\n  servlet:\n    multipart:\n      max-file-size: 10MB\n      max-request-size: 10MB\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://47.108.255.49:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    #url: jdbc:mysql://localhost:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    username: resume_king\n    password: HBMAFskbEzDMPjnc\nmybatis:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.example.resumebuilder.entity\n  configuration:\n    map-underscore-to-camel-case: true\n\naliyun:\n  oss:\n    endpoint: oss-cn-beijing.aliyuncs.com\n    accessKeyId: LTAI5t7Vr2TX5KzUtorBufb4\n    accessKeySecret: cic32QP7wzZm8UdqzKngFod00h9BED\n    bucketName: web-oss-learn\n    urlPrefix: https://web-oss-learn.oss-cn-beijing.aliyuncs.com/\n\ndeepseek:\n  api:\n    key: sk-92d8e843b30149969cb761e6f595f412\n    url: https://api.deepseek.com/v1/chat/completions\n\n# 雪花算法配置\nsnowflake:\n  datacenter-id: 1  # 数据中心ID，可根据实际部署环境调整\n  machine-id: 0     # 机器ID，设置为0将自动根据主机名生成\n  clock-backward-wait-time: 5000  # 时钟回拨最大等待时间(毫秒)\n  max-clock-backward-time: 1000   # 最大允许时钟回拨时间(毫秒)，超过此值使用借号方案    ', '1c05f48f63fe70d99ad346d7688b331b', '2025-07-09 22:22:01', '2025-07-09 22:22:02', NULL, '111.18.40.206', 'I', '', '');
INSERT INTO `his_config_info` (`id`, `nid`, `data_id`, `group_id`, `app_name`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `op_type`, `tenant_id`, `encrypted_data_key`) VALUES (1, 2, 'resume_king', 'DEFAULT_GROUP', '', 'spring:\n  servlet:\n    multipart:\n      max-file-size: 10MB\n      max-request-size: 10MB\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://47.108.255.49:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    #url: jdbc:mysql://localhost:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    username: resume_king\n    password: HBMAFskbEzDMPjnc\nmybatis:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.example.resumebuilder.entity\n  configuration:\n    map-underscore-to-camel-case: true\n\naliyun:\n  oss:\n    endpoint: oss-cn-beijing.aliyuncs.com\n    accessKeyId: LTAI5t7Vr2TX5KzUtorBufb4\n    accessKeySecret: cic32QP7wzZm8UdqzKngFod00h9BED\n    bucketName: web-oss-learn\n    urlPrefix: https://web-oss-learn.oss-cn-beijing.aliyuncs.com/\n\ndeepseek:\n  api:\n    key: sk-92d8e843b30149969cb761e6f595f412\n    url: https://api.deepseek.com/v1/chat/completions\n\n# 雪花算法配置\nsnowflake:\n  datacenter-id: 1  # 数据中心ID，可根据实际部署环境调整\n  machine-id: 0     # 机器ID，设置为0将自动根据主机名生成\n  clock-backward-wait-time: 5000  # 时钟回拨最大等待时间(毫秒)\n  max-clock-backward-time: 1000   # 最大允许时钟回拨时间(毫秒)，超过此值使用借号方案    ', '1c05f48f63fe70d99ad346d7688b331b', '2025-07-09 22:24:32', '2025-07-09 22:24:33', NULL, '111.18.40.252', 'U', '', '');
INSERT INTO `his_config_info` (`id`, `nid`, `data_id`, `group_id`, `app_name`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `op_type`, `tenant_id`, `encrypted_data_key`) VALUES (1, 3, 'resume_king', 'DEFAULT_GROUP', '', 'spring:\n  servlet:\n    multipart:\n      max-file-size: 10MB\n      max-request-size: 10MB\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://47.108.255.49:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    #url: jdbc:mysql://localhost:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    username: resume_king\n    password: 123456\nmybatis:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.example.resumebuilder.entity\n  configuration:\n    map-underscore-to-camel-case: true\n\naliyun:\n  oss:\n    endpoint: oss-cn-beijing.aliyuncs.com\n    accessKeyId: LTAI5t7Vr2TX5KzUtorBufb4\n    accessKeySecret: cic32QP7wzZm8UdqzKngFod00h9BED\n    bucketName: web-oss-learn\n    urlPrefix: https://web-oss-learn.oss-cn-beijing.aliyuncs.com/\n\ndeepseek:\n  api:\n    key: sk-92d8e843b30149969cb761e6f595f412\n    url: https://api.deepseek.com/v1/chat/completions\n\n# 雪花算法配置\nsnowflake:\n  datacenter-id: 1  # 数据中心ID，可根据实际部署环境调整\n  machine-id: 0     # 机器ID，设置为0将自动根据主机名生成\n  clock-backward-wait-time: 5000  # 时钟回拨最大等待时间(毫秒)\n  max-clock-backward-time: 1000   # 最大允许时钟回拨时间(毫秒)，超过此值使用借号方案    ', '27b06c6a71d750b0ec940df966266390', '2025-07-09 22:25:23', '2025-07-09 22:25:23', NULL, '111.18.40.252', 'U', '', '');
INSERT INTO `his_config_info` (`id`, `nid`, `data_id`, `group_id`, `app_name`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `op_type`, `tenant_id`, `encrypted_data_key`) VALUES (0, 4, 'nacos.cfg.dataIdfoo', 'foo', '', 'helloWorld', '1a833da63a6b7e20098dae06d06602e1', '2025-07-12 02:19:08', '2025-07-12 02:19:09', NULL, '194.102.39.128', 'I', '', '');
INSERT INTO `his_config_info` (`id`, `nid`, `data_id`, `group_id`, `app_name`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `op_type`, `tenant_id`, `encrypted_data_key`) VALUES (1, 5, 'resume_king', 'DEFAULT_GROUP', '', 'spring:\n  servlet:\n    multipart:\n      max-file-size: 10MB\n      max-request-size: 10MB\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://47.108.255.49:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    #url: jdbc:mysql://localhost:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    username: root\n    password: 123456\nmybatis:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.example.resumebuilder.entity\n  configuration:\n    map-underscore-to-camel-case: true\n\naliyun:\n  oss:\n    endpoint: oss-cn-beijing.aliyuncs.com\n    accessKeyId: LTAI5t7Vr2TX5KzUtorBufb4\n    accessKeySecret: cic32QP7wzZm8UdqzKngFod00h9BED\n    bucketName: web-oss-learn\n    urlPrefix: https://web-oss-learn.oss-cn-beijing.aliyuncs.com/\n\ndeepseek:\n  api:\n    key: sk-92d8e843b30149969cb761e6f595f412\n    url: https://api.deepseek.com/v1/chat/completions\n\n# 雪花算法配置\nsnowflake:\n  datacenter-id: 1  # 数据中心ID，可根据实际部署环境调整\n  machine-id: 0     # 机器ID，设置为0将自动根据主机名生成\n  clock-backward-wait-time: 5000  # 时钟回拨最大等待时间(毫秒)\n  max-clock-backward-time: 1000   # 最大允许时钟回拨时间(毫秒)，超过此值使用借号方案    ', '25470b02626ce09a461f7a29ac3ec381', '2025-07-13 16:54:46', '2025-07-13 16:54:46', NULL, '111.18.40.236', 'U', '', '');
INSERT INTO `his_config_info` (`id`, `nid`, `data_id`, `group_id`, `app_name`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `op_type`, `tenant_id`, `encrypted_data_key`) VALUES (2, 6, 'nacos.cfg.dataIdfoo', 'foo', '', 'helloWorld', '1a833da63a6b7e20098dae06d06602e1', '2025-07-14 16:22:42', '2025-07-14 16:22:42', NULL, '194.102.39.128', 'U', '', '');
INSERT INTO `his_config_info` (`id`, `nid`, `data_id`, `group_id`, `app_name`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `op_type`, `tenant_id`, `encrypted_data_key`) VALUES (1, 7, 'resume_king', 'DEFAULT_GROUP', '', 'spring:\n  servlet:\n    multipart:\n      max-file-size: 10MB\n      max-request-size: 10MB\n  datasource:\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    url: jdbc:mysql://47.108.255.49:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    #url: jdbc:mysql://localhost:3306/resume_king?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai\n    username: root\n    password: WTHwtx45\nmybatis:\n  mapper-locations: classpath:mapper/*.xml\n  type-aliases-package: com.example.resumebuilder.entity\n  configuration:\n    map-underscore-to-camel-case: true\n\naliyun:\n  oss:\n    endpoint: oss-cn-beijing.aliyuncs.com\n    accessKeyId: LTAI5t7Vr2TX5KzUtorBufb4\n    accessKeySecret: cic32QP7wzZm8UdqzKngFod00h9BED\n    bucketName: web-oss-learn\n    urlPrefix: https://web-oss-learn.oss-cn-beijing.aliyuncs.com/\n\ndeepseek:\n  api:\n    key: sk-92d8e843b30149969cb761e6f595f412\n    url: https://api.deepseek.com/v1/chat/completions\n\n# 雪花算法配置\nsnowflake:\n  datacenter-id: 1  # 数据中心ID，可根据实际部署环境调整\n  machine-id: 0     # 机器ID，设置为0将自动根据主机名生成\n  clock-backward-wait-time: 5000  # 时钟回拨最大等待时间(毫秒)\n  max-clock-backward-time: 1000   # 最大允许时钟回拨时间(毫秒)，超过此值使用借号方案    ', '98c9141a9d4aa5ea5d3d945789c9362a', '2025-07-14 19:24:29', '2025-07-14 19:24:30', NULL, '223.104.40.92', 'U', '', '');
COMMIT;

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'role',
  `resource` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'resource',
  `action` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'action',
  UNIQUE KEY `uk_role_permission` (`role`,`resource`,`action`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of permissions
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for resume_0
-- ----------------------------
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

-- ----------------------------
-- Records of resume_0
-- ----------------------------
BEGIN;
INSERT INTO `resume_0` (`id`, `content`, `create_time`, `update_time`, `status`, `like_count`, `user_id`, `title`, `is_template`, `template_source`, `publish_request_id`, `like`) VALUES (600244866370496409, '[{\"x\": 204, \"y\": 9, \"id\": \"1750000517688\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"简历王\", \"fontSize\": 16, \"textAlign\": \"center\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 150, \"y\": 37, \"id\": \"1750000566865\", \"type\": \"text-basic\", \"color\": \"#9c9292\", \"width\": 327, \"height\": 30, \"content\": \"888888888 |  888888888@qq.com\", \"fontSize\": 13, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 59, \"id\": \"1750000635495\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 100, \"height\": 30, \"content\": \"教育经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 74, \"id\": \"1750000706657\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 140, \"id\": \"1750000771446\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 601, \"id\": \"1750000786556\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 450, \"id\": \"1750000786966\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 751, \"id\": \"1750000787398\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 125, \"id\": \"1750000825601\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"实习经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11, \"y\": 434, \"id\": \"1750000871440\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"项目经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 14.1875, \"y\": 583, \"id\": \"1750128307015\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"专业技能\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 730, \"id\": \"1750128394259\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"技术积累\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 85, \"id\": \"1750128497564\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 177, \"height\": 30, \"content\": \"简历王大学 - 本科\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 84, \"id\": \"1750128520731\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 212, \"height\": 30, \"content\": \"2022年09月 - 2026年06月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 19.1875, \"y\": 105, \"id\": \"1750128556290\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"软件工程\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 18.359375, \"y\": 158, \"id\": \"1750128672377\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"小红书\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E5%25B0%258F%25E7%25BA%25A2%25E4%25B9%25A6-seeklogo.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 25, \"originalHeight\": 25}, {\"x\": 40.359375, \"y\": 156, \"id\": \"1750128684623\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 334, \"height\": 30, \"content\": \"小红书 - 行吟信息科技(北京)有限公司 - 后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 154, \"id\": \"1750128749416\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 171, \"height\": 30, \"content\": \"2025年02月 - 2025年05月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 163, \"id\": \"1750128792015\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 534, \"height\": 139, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 16.359375, \"y\": 304, \"id\": \"1750129203323\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"百度\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E7%2599%25BE%25E5%25BA%25A6_e53b9184-62f8-45bb-a65b-bcc753fbc87c.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 200, \"originalHeight\": 200}, {\"x\": 38.359375, \"y\": 302, \"id\": \"1750129267678\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 434, \"height\": 30, \"content\": \"百度 - 百度在线网络技术(北京)有限公司  -  后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 301, \"id\": \"1750129337479\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 209, \"height\": 30, \"content\": \"2024年11月 - 2025年02月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17, \"y\": 326, \"id\": \"1750129384207\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 535, \"height\": 116, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 619, \"id\": \"1750130041683\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 556, \"height\": 125, \"content\": \"• 熟练掌握Java核心知识，熟悉常用集合及数据结构、面向对象、反射、异常、类加载等；\\n• 熟练掌握MySQL，深刻理解事务及其原理、存储引擎、索引、锁机制、MVCC、各种日志等；\\n• 熟练掌握Redis，深刻理解其数据结构、持久化策略、IO模型、哨兵机制、高性能原理等；\\n• 熟悉JUC并发编程，熟悉掌握各种锁机制、CAS、AQS、线程池、ThreadLocal等实现原理；\\n• 熟练掌握JVM 内存结构、JMM、GC 算法、双亲委派机制、常见垃圾回收器等；\\n• 熟悉常见设计模式，如单例，工厂、责任链、策略模式等；\\n• 熟练使用消息中间件RabbitMQ，掌握lazy队列、死信队列等知识；\\n• 熟练使用SSM 、SpringBoot、SpringCloud等框架，深刻理解IOC、AOP、类加载流程等；\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11.359375, \"y\": 766, \"id\": \"1750130286395\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 551, \"height\": 35, \"content\": \"CSDN : mp9只想干开发  个人独立运营CSDN平台，分享近百篇分享技术心得和设计方案，阅读量3w+，粉丝量400+              CSDN链接： https://blog.csdn.net/2301_78947898?spm=1011.2266.3001.5343\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}]', '2025-07-15 16:38:48', '2025-07-15 16:38:48', 1, 0, NULL, '我的简历', 0, 'SYSTEM', NULL, 0);
INSERT INTO `resume_0` (`id`, `content`, `create_time`, `update_time`, `status`, `like_count`, `user_id`, `title`, `is_template`, `template_source`, `publish_request_id`, `like`) VALUES (600244889900541168, '[{\"x\": 204, \"y\": 9, \"id\": \"1750000517688\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"简历王\", \"fontSize\": 16, \"textAlign\": \"center\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 150, \"y\": 37, \"id\": \"1750000566865\", \"type\": \"text-basic\", \"color\": \"#9c9292\", \"width\": 327, \"height\": 30, \"content\": \"888888888 |  888888888@qq.com\", \"fontSize\": 13, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 59, \"id\": \"1750000635495\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 100, \"height\": 30, \"content\": \"教育经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 74, \"id\": \"1750000706657\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 140, \"id\": \"1750000771446\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 601, \"id\": \"1750000786556\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 450, \"id\": \"1750000786966\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 751, \"id\": \"1750000787398\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 125, \"id\": \"1750000825601\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"实习经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11, \"y\": 434, \"id\": \"1750000871440\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"项目经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 14.1875, \"y\": 583, \"id\": \"1750128307015\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"专业技能\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 730, \"id\": \"1750128394259\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"技术积累\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 85, \"id\": \"1750128497564\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 177, \"height\": 30, \"content\": \"简历王大学 - 本科\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 84, \"id\": \"1750128520731\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 212, \"height\": 30, \"content\": \"2022年09月 - 2026年06月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 19.1875, \"y\": 105, \"id\": \"1750128556290\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"软件工程\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 18.359375, \"y\": 158, \"id\": \"1750128672377\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"小红书\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E5%25B0%258F%25E7%25BA%25A2%25E4%25B9%25A6-seeklogo.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 25, \"originalHeight\": 25}, {\"x\": 40.359375, \"y\": 156, \"id\": \"1750128684623\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 334, \"height\": 30, \"content\": \"小红书 - 行吟信息科技(北京)有限公司 - 后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 154, \"id\": \"1750128749416\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 171, \"height\": 30, \"content\": \"2025年02月 - 2025年05月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 163, \"id\": \"1750128792015\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 534, \"height\": 139, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 16.359375, \"y\": 304, \"id\": \"1750129203323\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"百度\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E7%2599%25BE%25E5%25BA%25A6_e53b9184-62f8-45bb-a65b-bcc753fbc87c.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 200, \"originalHeight\": 200}, {\"x\": 38.359375, \"y\": 302, \"id\": \"1750129267678\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 434, \"height\": 30, \"content\": \"百度 - 百度在线网络技术(北京)有限公司  -  后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 301, \"id\": \"1750129337479\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 209, \"height\": 30, \"content\": \"2024年11月 - 2025年02月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17, \"y\": 326, \"id\": \"1750129384207\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 535, \"height\": 116, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 619, \"id\": \"1750130041683\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 556, \"height\": 125, \"content\": \"• 熟练掌握Java核心知识，熟悉常用集合及数据结构、面向对象、反射、异常、类加载等；\\n• 熟练掌握MySQL，深刻理解事务及其原理、存储引擎、索引、锁机制、MVCC、各种日志等；\\n• 熟练掌握Redis，深刻理解其数据结构、持久化策略、IO模型、哨兵机制、高性能原理等；\\n• 熟悉JUC并发编程，熟悉掌握各种锁机制、CAS、AQS、线程池、ThreadLocal等实现原理；\\n• 熟练掌握JVM 内存结构、JMM、GC 算法、双亲委派机制、常见垃圾回收器等；\\n• 熟悉常见设计模式，如单例，工厂、责任链、策略模式等；\\n• 熟练使用消息中间件RabbitMQ，掌握lazy队列、死信队列等知识；\\n• 熟练使用SSM 、SpringBoot、SpringCloud等框架，深刻理解IOC、AOP、类加载流程等；\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11.359375, \"y\": 766, \"id\": \"1750130286395\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 551, \"height\": 35, \"content\": \"CSDN : mp9只想干开发  个人独立运营CSDN平台，分享近百篇分享技术心得和设计方案，阅读量3w+，粉丝量400+              CSDN链接： https://blog.csdn.net/2301_78947898?spm=1011.2266.3001.5343\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}]', '2025-07-15 16:38:54', '2025-07-15 16:38:54', 1, 0, NULL, '我的简历', 0, 'SYSTEM', NULL, 0);
INSERT INTO `resume_0` (`id`, `content`, `create_time`, `update_time`, `status`, `like_count`, `user_id`, `title`, `is_template`, `template_source`, `publish_request_id`, `like`) VALUES (600244913568997446, '[{\"x\": 204, \"y\": 9, \"id\": \"1750000517688\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"简历王\", \"fontSize\": 16, \"textAlign\": \"center\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 150, \"y\": 37, \"id\": \"1750000566865\", \"type\": \"text-basic\", \"color\": \"#9c9292\", \"width\": 327, \"height\": 30, \"content\": \"888888888 |  888888888@qq.com\", \"fontSize\": 13, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 59, \"id\": \"1750000635495\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 100, \"height\": 30, \"content\": \"教育经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 74, \"id\": \"1750000706657\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 140, \"id\": \"1750000771446\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 601, \"id\": \"1750000786556\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 450, \"id\": \"1750000786966\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 751, \"id\": \"1750000787398\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 125, \"id\": \"1750000825601\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"实习经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11, \"y\": 434, \"id\": \"1750000871440\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"项目经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 14.1875, \"y\": 583, \"id\": \"1750128307015\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"专业技能\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 730, \"id\": \"1750128394259\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"技术积累\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 85, \"id\": \"1750128497564\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 177, \"height\": 30, \"content\": \"简历王大学 - 本科\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 84, \"id\": \"1750128520731\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 212, \"height\": 30, \"content\": \"2022年09月 - 2026年06月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 19.1875, \"y\": 105, \"id\": \"1750128556290\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"软件工程\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 18.359375, \"y\": 158, \"id\": \"1750128672377\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"小红书\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E5%25B0%258F%25E7%25BA%25A2%25E4%25B9%25A6-seeklogo.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 25, \"originalHeight\": 25}, {\"x\": 40.359375, \"y\": 156, \"id\": \"1750128684623\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 334, \"height\": 30, \"content\": \"小红书 - 行吟信息科技(北京)有限公司 - 后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 154, \"id\": \"1750128749416\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 171, \"height\": 30, \"content\": \"2025年02月 - 2025年05月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 163, \"id\": \"1750128792015\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 534, \"height\": 139, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 16.359375, \"y\": 304, \"id\": \"1750129203323\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"百度\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E7%2599%25BE%25E5%25BA%25A6_e53b9184-62f8-45bb-a65b-bcc753fbc87c.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 200, \"originalHeight\": 200}, {\"x\": 38.359375, \"y\": 302, \"id\": \"1750129267678\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 434, \"height\": 30, \"content\": \"百度 - 百度在线网络技术(北京)有限公司  -  后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 301, \"id\": \"1750129337479\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 209, \"height\": 30, \"content\": \"2024年11月 - 2025年02月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17, \"y\": 326, \"id\": \"1750129384207\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 535, \"height\": 116, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 619, \"id\": \"1750130041683\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 556, \"height\": 125, \"content\": \"• 熟练掌握Java核心知识，熟悉常用集合及数据结构、面向对象、反射、异常、类加载等；\\n• 熟练掌握MySQL，深刻理解事务及其原理、存储引擎、索引、锁机制、MVCC、各种日志等；\\n• 熟练掌握Redis，深刻理解其数据结构、持久化策略、IO模型、哨兵机制、高性能原理等；\\n• 熟悉JUC并发编程，熟悉掌握各种锁机制、CAS、AQS、线程池、ThreadLocal等实现原理；\\n• 熟练掌握JVM 内存结构、JMM、GC 算法、双亲委派机制、常见垃圾回收器等；\\n• 熟悉常见设计模式，如单例，工厂、责任链、策略模式等；\\n• 熟练使用消息中间件RabbitMQ，掌握lazy队列、死信队列等知识；\\n• 熟练使用SSM 、SpringBoot、SpringCloud等框架，深刻理解IOC、AOP、类加载流程等；\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11.359375, \"y\": 766, \"id\": \"1750130286395\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 551, \"height\": 35, \"content\": \"CSDN : mp9只想干开发  个人独立运营CSDN平台，分享近百篇分享技术心得和设计方案，阅读量3w+，粉丝量400+              CSDN链接： https://blog.csdn.net/2301_78947898?spm=1011.2266.3001.5343\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}]', '2025-07-15 16:38:59', '2025-07-15 16:38:59', 1, 0, NULL, '我的简历', 0, 'SYSTEM', NULL, 0);
INSERT INTO `resume_0` (`id`, `content`, `create_time`, `update_time`, `status`, `like_count`, `user_id`, `title`, `is_template`, `template_source`, `publish_request_id`, `like`) VALUES (600244924700682546, '[{\"x\": 204, \"y\": 9, \"id\": \"1750000517688\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"简历王\", \"fontSize\": 16, \"textAlign\": \"center\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 150, \"y\": 37, \"id\": \"1750000566865\", \"type\": \"text-basic\", \"color\": \"#9c9292\", \"width\": 327, \"height\": 30, \"content\": \"888888888 |  888888888@qq.com\", \"fontSize\": 13, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 59, \"id\": \"1750000635495\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 100, \"height\": 30, \"content\": \"教育经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 74, \"id\": \"1750000706657\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 140, \"id\": \"1750000771446\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 601, \"id\": \"1750000786556\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 450, \"id\": \"1750000786966\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 20, \"y\": 751, \"id\": \"1750000787398\", \"type\": \"divider-solid\", \"color\": \"#ff0000\", \"width\": 520, \"height\": 20, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false}, {\"x\": 19, \"y\": 125, \"id\": \"1750000825601\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"实习经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11, \"y\": 434, \"id\": \"1750000871440\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"项目经历\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 14.1875, \"y\": 583, \"id\": \"1750128307015\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"专业技能\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 730, \"id\": \"1750128394259\", \"type\": \"text-basic\", \"color\": \"#ff0000\", \"width\": 120, \"height\": 30, \"content\": \"技术积累\", \"fontSize\": 15, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.1875, \"y\": 85, \"id\": \"1750128497564\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 177, \"height\": 30, \"content\": \"简历王大学 - 本科\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 84, \"id\": \"1750128520731\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 212, \"height\": 30, \"content\": \"2022年09月 - 2026年06月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 19.1875, \"y\": 105, \"id\": \"1750128556290\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 120, \"height\": 30, \"content\": \"软件工程\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 18.359375, \"y\": 158, \"id\": \"1750128672377\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"小红书\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E5%25B0%258F%25E7%25BA%25A2%25E4%25B9%25A6-seeklogo.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 25, \"originalHeight\": 25}, {\"x\": 40.359375, \"y\": 156, \"id\": \"1750128684623\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 334, \"height\": 30, \"content\": \"小红书 - 行吟信息科技(北京)有限公司 - 后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 154, \"id\": \"1750128749416\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 171, \"height\": 30, \"content\": \"2025年02月 - 2025年05月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 163, \"id\": \"1750128792015\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 534, \"height\": 139, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 16.359375, \"y\": 304, \"id\": \"1750129203323\", \"type\": \"image\", \"color\": \"#000000\", \"width\": 22, \"height\": 22, \"fontSize\": 16, \"imageAlt\": \"百度\", \"imageUrl\": \"/api/proxy/image?url=https%3A%2F%2Fweb-oss-learn.oss-cn-beijing.aliyuncs.com%2F%25E7%2599%25BE%25E5%25BA%25A6_e53b9184-62f8-45bb-a65b-bcc753fbc87c.png\", \"fontFamily\": \"Microsoft YaHei\", \"isResizing\": false, \"originalWidth\": 200, \"originalHeight\": 200}, {\"x\": 38.359375, \"y\": 302, \"id\": \"1750129267678\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 434, \"height\": 30, \"content\": \"百度 - 百度在线网络技术(北京)有限公司  -  后端开发实习生\", \"fontSize\": 11, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 410, \"y\": 301, \"id\": \"1750129337479\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 209, \"height\": 30, \"content\": \"2024年11月 - 2025年02月\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17, \"y\": 326, \"id\": \"1750129384207\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 535, \"height\": 116, \"content\": \"\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 17.359375, \"y\": 619, \"id\": \"1750130041683\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 556, \"height\": 125, \"content\": \"• 熟练掌握Java核心知识，熟悉常用集合及数据结构、面向对象、反射、异常、类加载等；\\n• 熟练掌握MySQL，深刻理解事务及其原理、存储引擎、索引、锁机制、MVCC、各种日志等；\\n• 熟练掌握Redis，深刻理解其数据结构、持久化策略、IO模型、哨兵机制、高性能原理等；\\n• 熟悉JUC并发编程，熟悉掌握各种锁机制、CAS、AQS、线程池、ThreadLocal等实现原理；\\n• 熟练掌握JVM 内存结构、JMM、GC 算法、双亲委派机制、常见垃圾回收器等；\\n• 熟悉常见设计模式，如单例，工厂、责任链、策略模式等；\\n• 熟练使用消息中间件RabbitMQ，掌握lazy队列、死信队列等知识；\\n• 熟练使用SSM 、SpringBoot、SpringCloud等框架，深刻理解IOC、AOP、类加载流程等；\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 11.359375, \"y\": 766, \"id\": \"1750130286395\", \"type\": \"text-basic\", \"color\": \"#000000\", \"width\": 551, \"height\": 35, \"content\": \"CSDN : mp9只想干开发  个人独立运营CSDN平台，分享近百篇分享技术心得和设计方案，阅读量3w+，粉丝量400+              CSDN链接： https://blog.csdn.net/2301_78947898?spm=1011.2266.3001.5343\", \"fontSize\": 10, \"textAlign\": \"left\", \"fontFamily\": \"Microsoft YaHei\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}]', '2025-07-15 16:39:02', '2025-07-15 16:39:02', 1, 0, NULL, '我的简历', 0, 'SYSTEM', NULL, 0);
INSERT INTO `resume_0` (`id`, `content`, `create_time`, `update_time`, `status`, `like_count`, `user_id`, `title`, `is_template`, `template_source`, `publish_request_id`, `like`) VALUES (600679033679898543, '[{\"x\": 20, \"y\": 20, \"id\": \"1750130800001\", \"type\": \"text-basic\", \"color\": \"#2c3e50\", \"width\": 200, \"height\": 40, \"content\": \"李静\", \"fontSize\": 32, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.2}, {\"x\": 20, \"y\": 60, \"id\": \"1750130800002\", \"type\": \"text-basic\", \"color\": \"#3498db\", \"width\": 250, \"height\": 20, \"content\": \"数据科学家 | 人工智能爱好者\", \"fontSize\": 14, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 100, \"id\": \"1750130800003\", \"type\": \"text-basic\", \"color\": \"#34495e\", \"width\": 200, \"height\": 20, \"content\": \"联系方式\", \"fontSize\": 16, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 125, \"id\": \"1750130800004\", \"type\": \"divider-solid\", \"color\": \"#3498db\", \"width\": 180, \"height\": 2, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Helvetica Neue\", \"isResizing\": false}, {\"x\": 20, \"y\": 140, \"id\": \"1750130800005\", \"type\": \"text-basic\", \"color\": \"#7f8c8d\", \"width\": 200, \"height\": 60, \"content\": \"电话: 999999999\\n邮箱: lijing.ds@example.com\\nLinkedIn: linkedin.com/in/lijingds\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.6}, {\"x\": 20, \"y\": 220, \"id\": \"1750130800006\", \"type\": \"text-basic\", \"color\": \"#34495e\", \"width\": 200, \"height\": 20, \"content\": \"教育背景\", \"fontSize\": 16, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 245, \"id\": \"1750130800007\", \"type\": \"divider-solid\", \"color\": \"#3498db\", \"width\": 180, \"height\": 2, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Helvetica Neue\", \"isResizing\": false}, {\"x\": 20, \"y\": 260, \"id\": \"1750130800008\", \"type\": \"text-basic\", \"color\": \"#7f8c8d\", \"width\": 200, \"height\": 80, \"content\": \"理学硕士, 数据科学\\nABC大学\\n2024 - 2026\\n\\n理学学士, 统计学\\nXYZ大学\\n2020 - 2024\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.6}, {\"x\": 20, \"y\": 360, \"id\": \"1750130800009\", \"type\": \"text-basic\", \"color\": \"#34495e\", \"width\": 200, \"height\": 20, \"content\": \"专业技能\", \"fontSize\": 16, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 20, \"y\": 385, \"id\": \"1750130800010\", \"type\": \"divider-solid\", \"color\": \"#3498db\", \"width\": 180, \"height\": 2, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Helvetica Neue\", \"isResizing\": false}, {\"x\": 20, \"y\": 400, \"id\": \"1750130800011\", \"type\": \"text-basic\", \"color\": \"#7f8c8d\", \"width\": 200, \"height\": 150, \"content\": \"• Python (Pandas, NumPy)\\n• Scikit-learn, TensorFlow\\n• SQL, NoSQL\\n• Tableau, Power BI\\n• A/B 测试\\n• 机器学习模型\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.6}, {\"x\": 240, \"y\": 100, \"id\": \"1750130800012\", \"type\": \"text-basic\", \"color\": \"#34495e\", \"width\": 200, \"height\": 20, \"content\": \"工作经历\", \"fontSize\": 16, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 240, \"y\": 125, \"id\": \"1750130800013\", \"type\": \"divider-solid\", \"color\": \"#3498db\", \"width\": 380, \"height\": 2, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Helvetica Neue\", \"isResizing\": false}, {\"x\": 240, \"y\": 140, \"id\": \"1750130800014\", \"type\": \"text-basic\", \"color\": \"#2c3e50\", \"width\": 380, \"height\": 20, \"content\": \"数据分析实习生 | 某科技公司\", \"fontSize\": 14, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 520, \"y\": 142, \"id\": \"1750130800015\", \"type\": \"text-basic\", \"color\": \"#7f8c8d\", \"width\": 100, \"height\": 20, \"content\": \"2025.01 - 至今\", \"fontSize\": 10, \"textAlign\": \"right\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 240, \"y\": 170, \"id\": \"1750130800016\", \"type\": \"text-basic\", \"color\": \"#34495e\", \"width\": 380, \"height\": 100, \"content\": \"• 参与用户行为分析项目，通过数据挖掘用户偏好，为产品迭代提供数据支持，使关键指标提升15%。\\n• 使用Python进行数据清洗和预处理，构建预测模型，准确率达到90%以上。\\n• 制作数据可视化报告，向团队清晰展示分析结果和洞察。\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.8}, {\"x\": 240, \"y\": 300, \"id\": \"1750130800017\", \"type\": \"text-basic\", \"color\": \"#34495e\", \"width\": 200, \"height\": 20, \"content\": \"项目经历\", \"fontSize\": 16, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 240, \"y\": 325, \"id\": \"1750130800018\", \"type\": \"divider-solid\", \"color\": \"#3498db\", \"width\": 380, \"height\": 2, \"fontSize\": 16, \"thickness\": 2, \"fontFamily\": \"Helvetica Neue\", \"isResizing\": false}, {\"x\": 240, \"y\": 340, \"id\": \"1750130800019\", \"type\": \"text-basic\", \"color\": \"#2c3e50\", \"width\": 380, \"height\": 20, \"content\": \"客户流失预测模型\", \"fontSize\": 14, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 700, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 520, \"y\": 342, \"id\": \"1750130800020\", \"type\": \"text-basic\", \"color\": \"#7f8c8d\", \"width\": 100, \"height\": 20, \"content\": \"2024.09 - 2024.12\", \"fontSize\": 10, \"textAlign\": \"right\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.5}, {\"x\": 240, \"y\": 370, \"id\": \"1750130800021\", \"type\": \"text-basic\", \"color\": \"#34495e\", \"width\": 380, \"height\": 100, \"content\": \"• 独立负责的课程项目，旨在预测电信客户的流失可能性。\\n• 利用逻辑回归和随机森林算法，比较模型性能，并使用GridSearch进行超参数调优。\\n• 最终模型在测试集上达到了85%的AUC得分，成功识别了潜在的流失客户群体。\", \"fontSize\": 12, \"textAlign\": \"left\", \"fontFamily\": \"Helvetica Neue\", \"fontWeight\": 400, \"isResizing\": false, \"lineHeight\": 1.8}]', '2025-07-16 21:24:02', '2025-07-16 21:24:02', 1, 0, NULL, '我的简历', 0, 'SYSTEM', NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for resume_1
-- ----------------------------
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

-- ----------------------------
-- Records of resume_1
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for resume_2
-- ----------------------------
DROP TABLE IF EXISTS `resume_2`;
CREATE TABLE `resume_2` (
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

-- ----------------------------
-- Records of resume_2
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for resume_publish_request
-- ----------------------------
DROP TABLE IF EXISTS `resume_publish_request`;
CREATE TABLE `resume_publish_request` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `resume_id` bigint NOT NULL COMMENT '简历ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '简历标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '简历描述',
  `status` enum('PENDING','APPROVED','REJECTED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '审批状态',
  `submit_time` datetime NOT NULL COMMENT '提交时间',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approver_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批人ID',
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '拒绝理由',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resume_id` (`resume_id`) COMMENT '简历ID索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_submit_time` (`submit_time`) COMMENT '提交时间索引',
  KEY `idx_approver_id` (`approver_id`) COMMENT '审批人ID索引',
  KEY `idx_resume_publish_request_status_submit_time` (`status`,`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历发布申请表';

-- ----------------------------
-- Records of resume_publish_request
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'username',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'role',
  UNIQUE KEY `idx_user_role` (`username`,`role`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of roles
-- ----------------------------
BEGIN;
INSERT INTO `roles` (`username`, `role`) VALUES ('nacos', 'ROLE_ADMIN');
COMMIT;

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int unsigned NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int unsigned NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int unsigned NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int unsigned NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
  `max_aggr_size` int unsigned NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int unsigned NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='租户容量信息表';

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='tenant_info';

-- ----------------------------
-- Records of tenant_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'username',
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'password',
  `enabled` tinyint(1) NOT NULL COMMENT 'enabled',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO `users` (`username`, `password`, `enabled`) VALUES ('GGLW1V', '$2a$10$K2aKEypirfR1/xvKpRmu9OaqOurDtGZ9xsCpaoCuVZz8y6KDte0cu', 1);
INSERT INTO `users` (`username`, `password`, `enabled`) VALUES ('k1xa51lp', '$2a$10$RVsT5TpdApIQMGZqN7t2.u6pMZ1iOxC0QS6rfHNunN112Kxhjhhi.', 1);
INSERT INTO `users` (`username`, `password`, `enabled`) VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 1);
COMMIT;

-- ----------------------------
-- View structure for approval_dashboard
-- ----------------------------
DROP VIEW IF EXISTS `approval_dashboard`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `approval_dashboard` AS select `rpr`.`id` AS `request_id`,`rpr`.`resume_id` AS `resume_id`,`rpr`.`title` AS `title`,`rpr`.`description` AS `description`,`rpr`.`status` AS `status`,`rpr`.`submit_time` AS `submit_time`,`rpr`.`approve_time` AS `approve_time`,`rpr`.`approver_id` AS `approver_id`,`rpr`.`reject_reason` AS `reject_reason`,`au`.`username` AS `approver_name`,`r`.`like_count` AS `like_count`,`r`.`create_time` AS `resume_create_time` from ((`resume_publish_request` `rpr` left join `admin_user` `au` on((`rpr`.`approver_id` = `au`.`id`))) left join `resume` `r` on((`rpr`.`resume_id` = `r`.`id`))) order by `rpr`.`submit_time` desc;

-- ----------------------------
-- View structure for approval_statistics
-- ----------------------------
DROP VIEW IF EXISTS `approval_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `approval_statistics` AS select cast(`resume_publish_request`.`submit_time` as date) AS `date`,count(0) AS `total_requests`,sum((case when (`resume_publish_request`.`status` = 'PENDING') then 1 else 0 end)) AS `pending_count`,sum((case when (`resume_publish_request`.`status` = 'APPROVED') then 1 else 0 end)) AS `approved_count`,sum((case when (`resume_publish_request`.`status` = 'REJECTED') then 1 else 0 end)) AS `rejected_count`,sum((case when (`resume_publish_request`.`status` = 'CANCELLED') then 1 else 0 end)) AS `cancelled_count` from `resume_publish_request` group by cast(`resume_publish_request`.`submit_time` as date) order by `date` desc;

-- ----------------------------
-- Procedure structure for batch_approve_requests
-- ----------------------------
DROP PROCEDURE IF EXISTS `batch_approve_requests`;
delimiter ;;
CREATE PROCEDURE `batch_approve_requests`(IN request_ids TEXT,
    IN approver_id VARCHAR(36),
    IN operation ENUM('APPROVE', 'REJECT'),
    IN comment TEXT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE current_id VARCHAR(36);
    DECLARE id_cursor CURSOR FOR 
        SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(request_ids, ',', numbers.n), ',', -1))
        FROM (SELECT @row := @row + 1 as n FROM 
              (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
              (SELECT @row:=0) r) numbers
        WHERE numbers.n <= 1 + (LENGTH(request_ids) - LENGTH(REPLACE(request_ids, ',', '')));
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN id_cursor;
    
    read_loop: LOOP
        FETCH id_cursor INTO current_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        UPDATE resume_publish_request 
        SET status = IF(operation = 'APPROVE', 'APPROVED', 'REJECTED'),
            approver_id = approver_id,
            approve_time = NOW(),
            reject_reason = IF(operation = 'REJECT', comment, NULL)
        WHERE id = current_id AND status = 'PENDING';
        
    END LOOP;
    
    CLOSE id_cursor;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
