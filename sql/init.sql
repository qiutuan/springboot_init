-- ============================================================
-- qiutuan-all-powerful-springboot 初始化脚本
-- MySQL 8.0+ / utf8mb4
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for request_log
-- ----------------------------
DROP TABLE IF EXISTS `request_log`;
CREATE TABLE `request_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_id` varchar(64) NOT NULL COMMENT '请求唯一ID',
  `url` varchar(255) NOT NULL COMMENT '请求URL',
  `method` varchar(10) NOT NULL COMMENT '请求方法',
  `params` text NULL COMMENT '请求参数',
  `ip` varchar(64) NOT NULL COMMENT '请求IP',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `status` int NOT NULL COMMENT '响应状态码',
  `error_msg` text NULL COMMENT '错误信息',
  `cost_time` bigint NOT NULL COMMENT '耗时(毫秒)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_request_id`(`request_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '请求日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_account` varchar(256) NOT NULL COMMENT '账号',
  `user_password` varchar(512) NOT NULL COMMENT '密码(BCrypt)',
  `user_name` varchar(256) NULL DEFAULT NULL COMMENT '用户昵称',
  `user_avatar` varchar(1024) NULL DEFAULT NULL COMMENT '用户头像',
  `user_profile` varchar(512) NULL DEFAULT NULL COMMENT '用户简介',
  `user_role` varchar(256) NOT NULL DEFAULT 'user' COMMENT '用户角色',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(逻辑删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_account`(`user_account`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- 初始管理员账号：admin 密码：12345678 (BCrypt)
INSERT INTO `user` (`id`, `user_account`, `user_password`, `user_name`, `user_role`) VALUES
(1, 'admin', '$2a$10$oY7/M.VigH1jCszuK5KWe.N.42kK3P.U2B5tKq2X1vB8oP6k2Z9K2', '管理员', 'admin');

-- ----------------------------
-- Table structure for sys_role 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码（Sa-Token 角色标识）',
  `role_name` varchar(128) NOT NULL COMMENT '角色名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(逻辑删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_permission 权限表
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `permission_code` varchar(128) NOT NULL COMMENT '权限编码（如 user:list）',
  `permission_name` varchar(128) NOT NULL COMMENT '权限名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(逻辑删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_permission_code`(`permission_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_role`(`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_permission 角色权限关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_permission`(`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 种子数据
-- ----------------------------

-- 角色
INSERT INTO `sys_role` (`role_code`, `role_name`) VALUES
('admin', '管理员'),
('user', '普通用户'),
('ban', '封禁用户');

-- 权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`) VALUES
('user:list', '用户列表', NULL),
('user:add', '新增用户', NULL),
('user:update', '更新用户', NULL),
('user:delete', '删除用户', NULL),
('user:get', '查看用户', NULL);

-- 将权限和角色关联映射
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r, `sys_permission` p WHERE r.role_code = 'admin';

-- 将现有 user 表的角色同步到 RBAC 关联表
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u, `sys_role` r WHERE u.user_role = 'admin' AND r.role_code = 'admin';
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u, `sys_role` r WHERE u.user_role = 'user' AND r.role_code = 'user';
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u, `sys_role` r WHERE u.user_role = 'ban' AND r.role_code = 'ban';

SET FOREIGN_KEY_CHECKS = 1;
