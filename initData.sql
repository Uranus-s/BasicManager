-- =============================================
-- 基础管理系统初始化脚本
-- 说明: 初始化 RBAC 用户 / 角色 / 权限 / 部门 / 字典
-- 默认密码: 123456
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清空关联表
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_user_dept;
TRUNCATE TABLE sys_role_permission;

-- 清空业务表
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_dept;
TRUNCATE TABLE sys_dict;
TRUNCATE TABLE sys_dict_item;
TRUNCATE TABLE sys_config;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 设置自增起始值
-- =============================================

ALTER TABLE sys_user AUTO_INCREMENT = 100;
ALTER TABLE sys_role AUTO_INCREMENT = 100;
ALTER TABLE sys_permission AUTO_INCREMENT = 100;
ALTER TABLE sys_dept AUTO_INCREMENT = 100;
ALTER TABLE sys_dict AUTO_INCREMENT = 100;
ALTER TABLE sys_dict_item AUTO_INCREMENT = 100;

-- =============================================
-- 1 部门 sys_dept
-- =============================================

INSERT IGNORE INTO sys_dept
(id, create_time, update_time, parent_id, dept_name, leader, phone, sort)
VALUES
(1, NOW(), NOW(), NULL, '总部', 'CEO', '13800000000', 1),
(2, NOW(), NOW(), 1, '研发部', '研发经理', '13800000001', 2),
(3, NOW(), NOW(), 1, '运营部', '运营经理', '13800000002', 3),
(4, NOW(), NOW(), 1, '市场部', '市场经理', '13800000003', 4);

-- =============================================
-- 2 用户 sys_user
-- 密码: 123456 (BCrypt)
-- =============================================

INSERT IGNORE INTO sys_user
(id, create_time, update_time, username, password, nickname, phone, email, avatar, status)
VALUES
(1, NOW(), NOW(), 'admin',
'$2a$10$Sh90wnt0He0ICZgflz29JuW24XkIbbSFMkWcyVhJP.KSM8rmeKDxW',
'系统管理员', '13800000000', 'admin@system.com', '', 1),

(2, NOW(), NOW(), 'test',
'$2a$10$Sh90wnt0He0ICZgflz29JuW24XkIbbSFMkWcyVhJP.KSM8rmeKDxW',
'测试用户', '13800000001', 'test@system.com', '', 1);

-- =============================================
-- 3 角色 sys_role
-- =============================================

INSERT IGNORE INTO sys_role
(id, create_time, update_time, role_code, role_name, status, remark)
VALUES
(1, NOW(), NOW(), 'admin', '系统管理员', 1, '系统超级管理员'),
(2, NOW(), NOW(), 'user', '普通用户', 1, '普通用户'),
(3, NOW(), NOW(), 'dept_admin', '部门管理员', 1, '部门管理员');

-- =============================================
-- 4 用户角色 sys_user_role
-- =============================================

INSERT IGNORE INTO sys_user_role (user_id, role_id)
VALUES
(1, 1),
(2, 2);

-- =============================================
-- 5 用户部门 sys_user_dept
-- =============================================

INSERT IGNORE INTO sys_user_dept (user_id, dept_id)
VALUES
(1, 1),
(2, 2);

-- =============================================
-- 6 菜单权限 sys_permission
-- =============================================

-- 顶级菜单
INSERT IGNORE INTO sys_permission
(id, create_time, update_time, parent_id, name, type, path, component, permission, icon, sort, visible, status)
VALUES
(1, NOW(), NOW(), 0, '首页', 'MENU', '/dashboard', 'dashboard/index', 'dashboard:view', 'HomeFilled', 1, 1, 1),

(2, NOW(), NOW(), 0, '系统管理', 'MENU', '/system', 'system/index', 'system:view', 'Setting', 2, 1, 1);

-- 用户管理
INSERT IGNORE INTO sys_permission
(id, create_time, update_time, parent_id, name, type, path, component, permission, icon, sort, visible, status)
VALUES
(101, NOW(), NOW(), 2, '用户管理', 'MENU', '/system/user', 'system/user/index', 'system:user:list', 'User', 1, 1, 1),
(102, NOW(), NOW(), 101, '用户查询', 'BUTTON', '', '', 'system:user:query', '', 1, 1, 1),
(103, NOW(), NOW(), 101, '用户新增', 'BUTTON', '', '', 'system:user:add', '', 2, 1, 1),
(104, NOW(), NOW(), 101, '用户编辑', 'BUTTON', '', '', 'system:user:edit', '', 3, 1, 1),
(105, NOW(), NOW(), 101, '用户删除', 'BUTTON', '', '', 'system:user:delete', '', 4, 1, 1);

-- 角色管理
INSERT IGNORE INTO sys_permission
(id, create_time, update_time, parent_id, name, type, path, component, permission, icon, sort, visible, status)
VALUES
(111, NOW(), NOW(), 2, '角色管理', 'MENU', '/system/role', 'system/role/index', 'system:role:list', 'UserFilled', 2, 1, 1),
(112, NOW(), NOW(), 111, '角色查询', 'BUTTON', '', '', 'system:role:query', '', 1, 1, 1),
(113, NOW(), NOW(), 111, '角色新增', 'BUTTON', '', '', 'system:role:add', '', 2, 1, 1),
(114, NOW(), NOW(), 111, '角色编辑', 'BUTTON', '', '', 'system:role:edit', '', 3, 1, 1),
(115, NOW(), NOW(), 111, '角色删除', 'BUTTON', '', '', 'system:role:delete', '', 4, 1, 1);

-- 菜单管理
INSERT IGNORE INTO sys_permission
(id, create_time, update_time, parent_id, name, type, path, component, permission, icon, sort, visible, status)
VALUES
(121, NOW(), NOW(), 2, '菜单管理', 'MENU', '/system/menu', 'system/menu/index', 'system:menu:list', 'Menu', 3, 1, 1);

-- =============================================
-- 7 角色权限 sys_role_permission
-- =============================================

-- 管理员拥有全部权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 普通用户
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
VALUES
(2, 1),
(2, 101),
(2, 102);

-- =============================================
-- 8 字典 sys_dict
-- =============================================

INSERT IGNORE INTO sys_dict
(id, create_time, update_time, dict_code, dict_name, status)
VALUES
(1, NOW(), NOW(), 'user_status', '用户状态', 1),
(2, NOW(), NOW(), 'menu_type', '菜单类型', 1);

-- =============================================
-- 9 字典项 sys_dict_item
-- =============================================

INSERT IGNORE INTO sys_dict_item
(id, create_time, update_time, dict_id, item_value, item_label, sort, status)
VALUES
(1, NOW(), NOW(), 1, '0', '禁用', 1, 1),
(2, NOW(), NOW(), 1, '1', '正常', 2, 1),

(3, NOW(), NOW(), 2, 'MENU', '菜单', 1, 1),
(4, NOW(), NOW(), 2, 'BUTTON', '按钮', 2, 1);

-- =============================================
-- 10 系统配置 sys_config
-- =============================================

INSERT IGNORE INTO sys_config
(id, create_time, update_time, config_key, config_value, remark)
VALUES
(1, NOW(), NOW(), 'sys.user.initPassword', '123456', '初始化密码'),
(2, NOW(), NOW(), 'sys.title', '基础管理系统', '系统名称'),
(3, NOW(), NOW(), 'sys.login.tokenExpire', '720', 'token过期时间');

-- =============================================
-- 完成
-- =============================================

SELECT 'RBAC 初始化完成' AS message;

SET FOREIGN_KEY_CHECKS = 1;
