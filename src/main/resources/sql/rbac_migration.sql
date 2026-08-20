-- 非破坏性 RBAC 迁移脚本：仅创建缺失的 RBAC 表并回填，不影响已有 user/request_log 数据
-- 全新安装请直接执行 resources/sql/springboot_init.sql

CREATE TABLE IF NOT EXISTS sys_role (
  id bigint NOT NULL AUTO_INCREMENT,
  role_code varchar(64) NOT NULL COMMENT '角色编码',
  role_name varchar(128) NOT NULL COMMENT '角色名称',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
  id bigint NOT NULL AUTO_INCREMENT,
  permission_code varchar(128) NOT NULL COMMENT '权限编码',
  permission_name varchar(128) NOT NULL COMMENT '权限名称',
  parent_id bigint DEFAULT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_delete tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  role_id bigint NOT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id bigint NOT NULL AUTO_INCREMENT,
  role_id bigint NOT NULL,
  permission_id bigint NOT NULL,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

INSERT IGNORE INTO sys_role (role_code, role_name) VALUES
('admin', '管理员'), ('user', '普通用户'), ('ban', '封禁用户');

INSERT IGNORE INTO sys_permission (permission_code, permission_name, parent_id) VALUES
('user:list', '用户列表', NULL),
('user:add', '新增用户', NULL),
('user:update', '更新用户', NULL),
('user:delete', '删除用户', NULL),
('user:get', '查看用户', NULL);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p WHERE r.role_code = 'admin';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM user u, sys_role r WHERE u.user_role = 'admin' AND r.role_code = 'admin';
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM user u, sys_role r WHERE u.user_role = 'user' AND r.role_code = 'user';
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM user u, sys_role r WHERE u.user_role = 'ban' AND r.role_code = 'ban';

-- 为 user.user_account 补唯一索引（若不存在）
SET @idx_exists = (SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'user' AND index_name = 'uk_user_account');
SET @ddl = IF(@idx_exists = 0, 'ALTER TABLE user ADD UNIQUE KEY uk_user_account (user_account)', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
