-- ============================================================================
-- BasicProject 完整初始化脚本
-- 适用范围：仅用于全新环境。本脚本会删除并重建项目拥有的全部数据表。
-- 演示账号：admin / dept_admin / demo，默认密码均为 123456。
-- 安全提示：部署到非本地环境前必须修改所有演示账号密码。
-- ============================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS basic_project
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE basic_project;

SET FOREIGN_KEY_CHECKS = 0;

-- 先删除关联表，再删除主表，保证脚本可以在全新环境中重复执行。
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_user_dept;
DROP TABLE IF EXISTS sys_notice_target;
DROP TABLE IF EXISTS sys_notice;
DROP TABLE IF EXISTS ai_agent_action;
DROP TABLE IF EXISTS sys_dict_item;
DROP TABLE IF EXISTS sys_oper_log;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS sys_file;
DROP TABLE IF EXISTS SPRING_AI_CHAT_MEMORY;
DROP TABLE IF EXISTS ai_chat_message;
DROP TABLE IF EXISTS sys_config;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_dept;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_dict;
DROP TABLE IF EXISTS sys_user;

-- ============================================================================
-- 1. 表结构
-- ============================================================================

CREATE TABLE sys_user
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人',
    update_by   BIGINT       NULL COMMENT '更新人',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    username    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(128) NOT NULL COMMENT 'BCrypt 登录密码',
    nickname    VARCHAR(64)  NULL COMMENT '昵称',
    phone       VARCHAR(20)  NULL COMMENT '手机号',
    email       VARCHAR(100) NULL COMMENT '邮箱',
    avatar      VARCHAR(255) NULL COMMENT '头像',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    PRIMARY KEY (id),
    KEY idx_sys_user_username_deleted (username, deleted),
    KEY idx_sys_user_phone_deleted (phone, deleted),
    KEY idx_sys_user_status_deleted (status, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统用户表';

CREATE TABLE sys_role
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人',
    update_by   BIGINT       NULL COMMENT '更新人',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    role_code   VARCHAR(64)  NOT NULL COMMENT '角色编码',
    role_name   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    remark      VARCHAR(255) NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sys_role_code_deleted (role_code, deleted),
    KEY idx_sys_role_status_deleted (status, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统角色表';

CREATE TABLE sys_permission
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人',
    update_by   BIGINT       NULL COMMENT '更新人',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父权限ID，0表示根节点',
    name        VARCHAR(64)  NOT NULL COMMENT '名称',
    type        VARCHAR(20)  NOT NULL COMMENT '类型 MENU/BUTTON/API',
    path        VARCHAR(255) NULL COMMENT '前端路由或接口路径',
    component   VARCHAR(255) NULL COMMENT '前端组件路径',
    permission  VARCHAR(128) NULL COMMENT '权限标识',
    icon        VARCHAR(100) NULL COMMENT '图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否显示 0=隐藏 1=显示',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    PRIMARY KEY (id),
    KEY idx_sys_permission_parent_sort (parent_id, sort),
    KEY idx_sys_permission_code_deleted (permission, deleted),
    KEY idx_sys_permission_status_visible (status, visible, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '菜单权限表';

CREATE TABLE sys_dept
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人',
    update_by   BIGINT       NULL COMMENT '更新人',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门ID，0表示根节点',
    dept_name   VARCHAR(100) NOT NULL COMMENT '部门名称',
    leader      VARCHAR(50)  NULL COMMENT '负责人',
    phone       VARCHAR(20)  NULL COMMENT '联系电话',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    KEY idx_sys_dept_parent_sort (parent_id, sort),
    KEY idx_sys_dept_name_deleted (dept_name, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '部门表';

CREATE TABLE sys_dict
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人',
    update_by   BIGINT       NULL COMMENT '更新人',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    dict_code   VARCHAR(100) NOT NULL COMMENT '字典编码',
    dict_name   VARCHAR(100) NOT NULL COMMENT '字典名称',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    PRIMARY KEY (id),
    KEY idx_sys_dict_code_deleted (dict_code, deleted),
    KEY idx_sys_dict_status_deleted (status, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统字典表';

CREATE TABLE sys_dict_item
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人',
    update_by   BIGINT       NULL COMMENT '更新人',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    dict_id     BIGINT       NOT NULL COMMENT '字典ID',
    item_value  VARCHAR(100) NOT NULL COMMENT '字典值',
    item_label  VARCHAR(100) NOT NULL COMMENT '字典标签',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    PRIMARY KEY (id),
    KEY idx_sys_dict_item_dict_sort (dict_id, sort),
    KEY idx_sys_dict_item_value_deleted (dict_id, item_value, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '字典项表';

CREATE TABLE sys_notice
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME     NOT NULL COMMENT '创建时间',
    update_time  DATETIME     NOT NULL COMMENT '更新时间',
    create_by    BIGINT       NULL COMMENT '创建人',
    update_by    BIGINT       NULL COMMENT '更新人',
    version      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    title        VARCHAR(200) NOT NULL COMMENT '公告标题',
    notice_type  VARCHAR(64)  NOT NULL COMMENT '公告类型字典项值',
    content      LONGTEXT     NOT NULL COMMENT 'Markdown 公告正文',
    scope_type   VARCHAR(16)  NOT NULL COMMENT '接收范围 ALL/TARGETED',
    status       VARCHAR(16)  NOT NULL COMMENT '状态 DRAFT/PUBLISHED/WITHDRAWN',
    publish_time DATETIME     NULL COMMENT '最近一次发布时间',
    PRIMARY KEY (id),
    KEY idx_sys_notice_status_publish_time (status, publish_time, deleted),
    KEY idx_sys_notice_type_status (notice_type, status, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '通知公告表';

CREATE TABLE sys_notice_target
(
    notice_id  BIGINT      NOT NULL COMMENT '公告ID',
    target_type VARCHAR(16) NOT NULL COMMENT '目标类型 ROLE/DEPT',
    target_id   BIGINT      NOT NULL COMMENT '角色或部门ID',
    PRIMARY KEY (notice_id, target_type, target_id),
    KEY idx_sys_notice_target_lookup (target_type, target_id, notice_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '公告接收目标关联表';

CREATE TABLE sys_config
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME     NULL COMMENT '创建时间',
    update_time  DATETIME     NULL COMMENT '更新时间',
    create_by    BIGINT       NULL COMMENT '创建人',
    update_by    BIGINT       NULL COMMENT '更新人',
    version      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    config_key   VARCHAR(100) NOT NULL COMMENT '参数键',
    config_value VARCHAR(255) NULL COMMENT '参数值',
    remark       VARCHAR(255) NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_config_key_deleted (config_key, deleted)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统参数表';

CREATE TABLE ai_chat_message
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME    NULL COMMENT '创建时间',
    update_time DATETIME    NULL COMMENT '更新时间',
    create_by   BIGINT      NULL COMMENT '创建人',
    update_by   BIGINT      NULL COMMENT '更新人',
    version     INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    user_id     BIGINT      NOT NULL COMMENT '所属用户ID',
    role        VARCHAR(16) NOT NULL COMMENT '消息角色 USER/ASSISTANT',
    content     LONGTEXT    NOT NULL COMMENT '消息正文',
    partial     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否为停止或异常后的部分回答',
    PRIMARY KEY (id),
    KEY idx_ai_chat_message_user_deleted_id (user_id, deleted, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI聊天消息表';

-- 写操作先固化为不可变快照；用户确认后才由确定性业务代码读取并执行。
CREATE TABLE ai_agent_action
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time  DATETIME     NULL COMMENT '创建时间',
    update_time  DATETIME     NULL COMMENT '更新时间',
    create_by    BIGINT       NULL COMMENT '创建人',
    update_by    BIGINT       NULL COMMENT '更新人',
    version      INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',
    trigger_message_id BIGINT       NOT NULL COMMENT '生成该操作的聊天消息ID',
    user_id      BIGINT       NOT NULL COMMENT '操作所属用户ID',
    action_type  VARCHAR(64)  NOT NULL COMMENT '操作类型',
    payload_json LONGTEXT     NOT NULL COMMENT '不可变业务快照JSON',
    status       VARCHAR(32)  NOT NULL COMMENT '审批状态',
    expires_at   DATETIME     NOT NULL COMMENT '审批过期时间',
    confirmed_at DATETIME     NULL COMMENT '确认时间',
    executed_at  DATETIME     NULL COMMENT '执行完成时间',
    result_id    BIGINT       NULL COMMENT '确定性执行结果业务ID',
    PRIMARY KEY (id),
    KEY idx_ai_agent_action_user_status_expire (user_id, status, expires_at, deleted),
    KEY idx_ai_agent_action_trigger_message (trigger_message_id, deleted)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'AI Agent待审批操作表';

CREATE TABLE SPRING_AI_CHAT_MEMORY
(
    `conversation_id` VARCHAR(36)                                      NOT NULL,
    `content`         TEXT                                             NOT NULL,
    `type`            ENUM('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')      NOT NULL,
    `timestamp`       TIMESTAMP                                        NOT NULL,
    `sequence_id`     BIGINT                                           NOT NULL,
    INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX` (`conversation_id`, `timestamp`),
    INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX` (`conversation_id`, `sequence_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Spring AI JDBC 聊天记忆表';

CREATE TABLE sys_file
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    create_by   BIGINT       NULL COMMENT '创建人',
    file_name   VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path   VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size   BIGINT       NULL COMMENT '文件大小（字节）',
    file_type   VARCHAR(100) NULL COMMENT '文件类型',
    biz_type    VARCHAR(50)  NULL COMMENT '业务类型',
    PRIMARY KEY (id),
    KEY idx_sys_file_biz_create_time (biz_type, create_time),
    KEY idx_sys_file_create_by (create_by)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '文件表';

CREATE TABLE sys_login_log
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time DATETIME     NULL COMMENT '创建时间',
    username    VARCHAR(64)  NULL COMMENT '用户名',
    ip          VARCHAR(64)  NULL COMMENT 'IP地址',
    browser     VARCHAR(100) NULL COMMENT '浏览器',
    os          VARCHAR(100) NULL COMMENT '操作系统',
    status      TINYINT      NULL COMMENT '状态 0=失败 1=成功',
    msg         VARCHAR(255) NULL COMMENT '提示消息',
    PRIMARY KEY (id),
    KEY idx_sys_login_log_create_time (create_time),
    KEY idx_sys_login_log_username (username),
    KEY idx_sys_login_log_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '登录日志表';

CREATE TABLE sys_oper_log
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    create_time     DATETIME     NULL COMMENT '创建时间',
    module          VARCHAR(100) NULL COMMENT '模块名',
    method          VARCHAR(100) NULL COMMENT '方法名',
    request_url     VARCHAR(255) NULL COMMENT '请求URL',
    request_method  VARCHAR(20)  NULL COMMENT '请求方式',
    request_params  TEXT         NULL COMMENT '请求参数',
    response_result TEXT         NULL COMMENT '返回结果',
    status          TINYINT      NULL COMMENT '状态 0=失败 1=成功',
    cost_time       BIGINT       NULL COMMENT '耗时（毫秒）',
    PRIMARY KEY (id),
    KEY idx_sys_oper_log_create_time (create_time),
    KEY idx_sys_oper_log_module (module),
    KEY idx_sys_oper_log_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '操作日志表';

CREATE TABLE sys_user_role
(
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户角色关联表';

CREATE TABLE sys_role_permission
(
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id),
    KEY idx_sys_role_permission_permission_id (permission_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '角色权限关联表';

CREATE TABLE sys_user_dept
(
    user_id BIGINT NOT NULL COMMENT '用户ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    PRIMARY KEY (user_id, dept_id),
    KEY idx_sys_user_dept_dept_id (dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户部门关联表';

-- ============================================================================
-- 2. 组织、账号与角色
-- ============================================================================

INSERT INTO sys_dept
    (id, create_time, update_time, create_by, update_by, version, deleted,
     parent_id, dept_name, leader, phone, sort)
VALUES
    (1, NOW(), NOW(), 1, 1, 0, 0, 0, '总部', '系统管理员', '13800000000', 1),
    (2, NOW(), NOW(), 1, 1, 0, 0, 1, '研发部', '研发经理', '13800000001', 1),
    (3, NOW(), NOW(), 1, 1, 0, 0, 1, '运营部', '运营经理', '13800000002', 2),
    (4, NOW(), NOW(), 1, 1, 0, 0, 1, '市场部', '市场经理', '13800000003', 3);

-- 默认密码均为 123456，仅供本地初始化使用。
INSERT INTO sys_user
    (id, create_time, update_time, create_by, update_by, version, deleted,
     username, password, nickname, phone, email, avatar, status)
VALUES
    (1, NOW(), NOW(), 1, 1, 0, 0, 'admin',
     '$2a$10$Sh90wnt0He0ICZgflz29JuW24XkIbbSFMkWcyVhJP.KSM8rmeKDxW',
     '系统管理员', '13800000000', 'admin@example.com', NULL, 1),
    (2, NOW(), NOW(), 1, 1, 0, 0, 'dept_admin',
     '$2a$10$Sh90wnt0He0ICZgflz29JuW24XkIbbSFMkWcyVhJP.KSM8rmeKDxW',
     '部门管理员', '13800000001', 'dept.admin@example.com', NULL, 1),
    (3, NOW(), NOW(), 1, 1, 0, 0, 'demo',
     '$2a$10$Sh90wnt0He0ICZgflz29JuW24XkIbbSFMkWcyVhJP.KSM8rmeKDxW',
     '演示用户', '13800000002', 'demo@example.com', NULL, 1);

INSERT INTO sys_role
    (id, create_time, update_time, create_by, update_by, version, deleted,
     role_code, role_name, status, remark)
VALUES
    (1, NOW(), NOW(), 1, 1, 0, 0, 'admin', '系统管理员', 1, '拥有系统全部权限'),
    (2, NOW(), NOW(), 1, 1, 0, 0, 'dept_admin', '部门管理员', 1, '拥有受限的用户与部门管理权限'),
    (3, NOW(), NOW(), 1, 1, 0, 0, 'user', '普通用户', 1, '仅使用首页和个人中心');

INSERT INTO sys_user_role (user_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO sys_user_dept (user_id, dept_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

-- ============================================================================
-- 3. 菜单与接口权限
-- ============================================================================

INSERT INTO sys_permission
    (id, create_time, update_time, create_by, update_by, version, deleted,
     parent_id, name, type, path, component, permission, icon, sort, visible, status)
VALUES
    -- 系统管理根菜单
    (1, NOW(), NOW(), 1, 1, 0, 0, 0, '系统管理', 'MENU', '/system', NULL,
     'system:view', 'Setting', 1, 1, 1),

    -- 用户管理
    (100, NOW(), NOW(), 1, 1, 0, 0, 1, '用户管理', 'MENU', '/system/user',
     'system/user/index.vue', 'system:user:list', 'User', 1, 1, 1),
    (101, NOW(), NOW(), 1, 1, 0, 0, 100, '查询用户', 'BUTTON', NULL, NULL,
     'system:user:query', NULL, 1, 0, 1),
    (102, NOW(), NOW(), 1, 1, 0, 0, 100, '新增用户', 'BUTTON', NULL, NULL,
     'system:user:add', NULL, 2, 0, 1),
    (103, NOW(), NOW(), 1, 1, 0, 0, 100, '修改用户', 'BUTTON', NULL, NULL,
     'system:user:edit', NULL, 3, 0, 1),
    (104, NOW(), NOW(), 1, 1, 0, 0, 100, '删除用户', 'BUTTON', NULL, NULL,
     'system:user:delete', NULL, 4, 0, 1),
    (105, NOW(), NOW(), 1, 1, 0, 0, 100, '重置用户密码', 'BUTTON', NULL, NULL,
     'system:user:resetPwd', NULL, 5, 0, 1),
    (106, NOW(), NOW(), 1, 1, 0, 0, 100, '分配用户角色', 'BUTTON', NULL, NULL,
     'system:user:assignRole', NULL, 6, 0, 1),

    -- 角色管理
    (200, NOW(), NOW(), 1, 1, 0, 0, 1, '角色管理', 'MENU', '/system/role',
     'system/role/index.vue', 'system:role:list', 'UserFilled', 2, 1, 1),
    (201, NOW(), NOW(), 1, 1, 0, 0, 200, '查询角色', 'BUTTON', NULL, NULL,
     'system:role:query', NULL, 1, 0, 1),
    (202, NOW(), NOW(), 1, 1, 0, 0, 200, '新增角色', 'BUTTON', NULL, NULL,
     'system:role:add', NULL, 2, 0, 1),
    (203, NOW(), NOW(), 1, 1, 0, 0, 200, '修改角色', 'BUTTON', NULL, NULL,
     'system:role:edit', NULL, 3, 0, 1),
    (204, NOW(), NOW(), 1, 1, 0, 0, 200, '删除角色', 'BUTTON', NULL, NULL,
     'system:role:delete', NULL, 4, 0, 1),
    (205, NOW(), NOW(), 1, 1, 0, 0, 200, '分配角色权限', 'BUTTON', NULL, NULL,
     'system:role:assignPermission', NULL, 5, 0, 1),
    (206, NOW(), NOW(), 1, 1, 0, 0, 200, '管理角色用户', 'BUTTON', NULL, NULL,
     'system:role:assignUser', NULL, 6, 0, 1),

    -- 权限管理
    (300, NOW(), NOW(), 1, 1, 0, 0, 1, '权限管理', 'MENU', '/system/permission',
     'system/permission/index.vue', 'system:permission:list', 'Menu', 3, 1, 1),
    (301, NOW(), NOW(), 1, 1, 0, 0, 300, '查询权限', 'BUTTON', NULL, NULL,
     'system:permission:query', NULL, 1, 0, 1),
    (302, NOW(), NOW(), 1, 1, 0, 0, 300, '新增权限', 'BUTTON', NULL, NULL,
     'system:permission:add', NULL, 2, 0, 1),
    (303, NOW(), NOW(), 1, 1, 0, 0, 300, '修改权限', 'BUTTON', NULL, NULL,
     'system:permission:edit', NULL, 3, 0, 1),
    (304, NOW(), NOW(), 1, 1, 0, 0, 300, '删除权限', 'BUTTON', NULL, NULL,
     'system:permission:delete', NULL, 4, 0, 1),

    -- 部门管理
    (400, NOW(), NOW(), 1, 1, 0, 0, 1, '部门管理', 'MENU', '/system/dept',
     'system/dept/index.vue', 'system:dept:list', 'OfficeBuilding', 4, 1, 1),
    (401, NOW(), NOW(), 1, 1, 0, 0, 400, '查询部门', 'BUTTON', NULL, NULL,
     'system:dept:query', NULL, 1, 0, 1),
    (402, NOW(), NOW(), 1, 1, 0, 0, 400, '新增部门', 'BUTTON', NULL, NULL,
     'system:dept:add', NULL, 2, 0, 1),
    (403, NOW(), NOW(), 1, 1, 0, 0, 400, '修改部门', 'BUTTON', NULL, NULL,
     'system:dept:edit', NULL, 3, 0, 1),
    (404, NOW(), NOW(), 1, 1, 0, 0, 400, '删除部门', 'BUTTON', NULL, NULL,
     'system:dept:delete', NULL, 4, 0, 1),
    (405, NOW(), NOW(), 1, 1, 0, 0, 400, '管理部门用户', 'BUTTON', NULL, NULL,
     'system:dept:assignUser', NULL, 5, 0, 1),

    -- 字典管理
    (500, NOW(), NOW(), 1, 1, 0, 0, 1, '字典管理', 'MENU', '/system/dict',
     'system/dict/index.vue', 'system:dict:list', 'Collection', 5, 1, 1),
    (501, NOW(), NOW(), 1, 1, 0, 0, 500, '查询字典', 'BUTTON', NULL, NULL,
     'system:dict:query', NULL, 1, 0, 1),
    (502, NOW(), NOW(), 1, 1, 0, 0, 500, '新增字典', 'BUTTON', NULL, NULL,
     'system:dict:add', NULL, 2, 0, 1),
    (503, NOW(), NOW(), 1, 1, 0, 0, 500, '修改字典', 'BUTTON', NULL, NULL,
     'system:dict:edit', NULL, 3, 0, 1),
    (504, NOW(), NOW(), 1, 1, 0, 0, 500, '删除字典', 'BUTTON', NULL, NULL,
     'system:dict:delete', NULL, 4, 0, 1),
    (511, NOW(), NOW(), 1, 1, 0, 0, 500, '查询字典项', 'BUTTON', NULL, NULL,
     'system:dict:item:query', NULL, 11, 0, 1),
    (512, NOW(), NOW(), 1, 1, 0, 0, 500, '新增字典项', 'BUTTON', NULL, NULL,
     'system:dict:item:add', NULL, 12, 0, 1),
    (513, NOW(), NOW(), 1, 1, 0, 0, 500, '修改字典项', 'BUTTON', NULL, NULL,
     'system:dict:item:edit', NULL, 13, 0, 1),
    (514, NOW(), NOW(), 1, 1, 0, 0, 500, '删除字典项', 'BUTTON', NULL, NULL,
     'system:dict:item:delete', NULL, 14, 0, 1),

    -- 日志管理：前端使用统一日志页面，独立日志接口保留细粒度操作权限。
    (600, NOW(), NOW(), 1, 1, 0, 0, 1, '系统日志', 'MENU', '/system/log',
     'system/log/index.vue', 'system:log:list', 'Document', 6, 1, 1),
    (601, NOW(), NOW(), 1, 1, 0, 0, 600, '查询综合日志', 'BUTTON', NULL, NULL,
     'system:log:query', NULL, 1, 0, 1),
    (610, NOW(), NOW(), 1, 1, 0, 0, 600, '查询登录日志', 'BUTTON', NULL, NULL,
     'system:loginLog:query', NULL, 10, 0, 1),
    (611, NOW(), NOW(), 1, 1, 0, 0, 600, '删除登录日志', 'BUTTON', NULL, NULL,
     'system:loginLog:delete', NULL, 11, 0, 1),
    (612, NOW(), NOW(), 1, 1, 0, 0, 600, '批量删除登录日志', 'BUTTON', NULL, NULL,
     'system:loginLog:batchDelete', NULL, 12, 0, 1),
    (613, NOW(), NOW(), 1, 1, 0, 0, 600, '清空登录日志', 'BUTTON', NULL, NULL,
     'system:loginLog:clear', NULL, 13, 0, 1),
    (620, NOW(), NOW(), 1, 1, 0, 0, 600, '查询操作日志', 'BUTTON', NULL, NULL,
     'system:operLog:query', NULL, 20, 0, 1),
    (621, NOW(), NOW(), 1, 1, 0, 0, 600, '删除操作日志', 'BUTTON', NULL, NULL,
     'system:operLog:delete', NULL, 21, 0, 1),
    (622, NOW(), NOW(), 1, 1, 0, 0, 600, '批量删除操作日志', 'BUTTON', NULL, NULL,
     'system:operLog:batchDelete', NULL, 22, 0, 1),
    (623, NOW(), NOW(), 1, 1, 0, 0, 600, '清空操作日志', 'BUTTON', NULL, NULL,
     'system:operLog:clear', NULL, 23, 0, 1),

    -- 在线用户与系统监控
    (700, NOW(), NOW(), 1, 1, 0, 0, 1, '在线用户', 'MENU', '/system/online',
     'system/online/index.vue', 'auth:online:list', 'Connection', 7, 1, 1),
    (701, NOW(), NOW(), 1, 1, 0, 0, 700, '强制用户下线', 'BUTTON', NULL, NULL,
     'auth:online:forceLogout', NULL, 1, 0, 1),
    (800, NOW(), NOW(), 1, 1, 0, 0, 1, '系统监控', 'MENU', '/system/monitor',
     'system/monitor/index.vue', 'system:monitor:view', 'Monitor', 8, 1, 1),

    -- 系统设置只允许查看和修改预定义业务设置。
    (900, NOW(), NOW(), 1, 1, 0, 0, 1, '系统设置', 'MENU', '/system/config',
     'system/config/index.vue', 'system:config:query', 'Tools', 9, 1, 1),
    (901, NOW(), NOW(), 1, 1, 0, 0, 900, '修改系统设置', 'BUTTON', NULL, NULL,
     'system:config:edit', NULL, 1, 0, 1),
    (910, NOW(), NOW(), 1, 1, 0, 0, 1, '查询文件', 'BUTTON', NULL, NULL,
     'system:file:query', NULL, 94, 0, 1),
    (911, NOW(), NOW(), 1, 1, 0, 0, 1, '上传文件', 'BUTTON', NULL, NULL,
     'system:file:upload', NULL, 95, 0, 1),
    (912, NOW(), NOW(), 1, 1, 0, 0, 1, '删除文件', 'BUTTON', NULL, NULL,
     'system:file:delete', NULL, 96, 0, 1),

    -- 通知公告
    (1000, NOW(), NOW(), 1, 1, 0, 0, 1, '通知公告', 'MENU', '/system/notice',
     'system/notice/index.vue', 'system:notice:list', 'Bell', 10, 1, 1),
    (1001, NOW(), NOW(), 1, 1, 0, 0, 1000, '查询公告', 'BUTTON', NULL, NULL,
     'system:notice:query', NULL, 1, 0, 1),
    (1002, NOW(), NOW(), 1, 1, 0, 0, 1000, '新增公告', 'BUTTON', NULL, NULL,
     'system:notice:add', NULL, 2, 0, 1),
    (1003, NOW(), NOW(), 1, 1, 0, 0, 1000, '修改公告', 'BUTTON', NULL, NULL,
     'system:notice:edit', NULL, 3, 0, 1),
    (1004, NOW(), NOW(), 1, 1, 0, 0, 1000, '删除公告', 'BUTTON', NULL, NULL,
     'system:notice:delete', NULL, 4, 0, 1),
    (1005, NOW(), NOW(), 1, 1, 0, 0, 1000, '发布公告', 'BUTTON', NULL, NULL,
     'system:notice:publish', NULL, 5, 0, 1),
    (1006, NOW(), NOW(), 1, 1, 0, 0, 1000, '撤回公告', 'BUTTON', NULL, NULL,
     'system:notice:withdraw', NULL, 6, 0, 1),

    -- 普通用户基础权限用于保证登录后的权限集合非空，不参与动态路由生成。
    (999, NOW(), NOW(), 1, 1, 0, 0, 0, '个人基础功能', 'BUTTON', NULL, NULL,
     'system:profile:view', NULL, 999, 0, 1);

-- 管理员拥有全部有效权限。
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id
FROM sys_permission
WHERE deleted = 0
  AND status = 1;

-- 部门管理员仅拥有受限的用户查询和部门管理能力，避免通过角色分配提升权限。
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES
    (2, 1),
    (2, 100),
    (2, 101),
    (2, 201),
    (2, 400),
    (2, 401),
    (2, 402),
    (2, 403),
    (2, 405),
    (2, 501),
    (2, 511),
    (2, 999);

-- 普通用户只使用前端常量首页与个人中心。
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES (3, 999);

-- ============================================================================
-- 4. 字典与系统参数
-- ============================================================================

INSERT INTO sys_dict
    (id, create_time, update_time, create_by, update_by, version, deleted,
     dict_code, dict_name, status)
VALUES
    (1, NOW(), NOW(), 1, 1, 0, 0, 'sys_user_status', '用户状态', 1),
    (2, NOW(), NOW(), 1, 1, 0, 0, 'sys_common_status', '通用状态', 1),
    (3, NOW(), NOW(), 1, 1, 0, 0, 'sys_permission_type', '权限类型', 1),
    (4, NOW(), NOW(), 1, 1, 0, 0, 'sys_log_type', '日志类型', 1),
    (5, NOW(), NOW(), 1, 1, 0, 0, 'sys_notice_type', '公告类型', 1);

INSERT INTO sys_dict_item
    (id, create_time, update_time, create_by, update_by, version, deleted,
     dict_id, item_value, item_label, sort, status)
VALUES
    (1, NOW(), NOW(), 1, 1, 0, 0, 1, '0', '禁用', 1, 1),
    (2, NOW(), NOW(), 1, 1, 0, 0, 1, '1', '正常', 2, 1),
    (3, NOW(), NOW(), 1, 1, 0, 0, 2, '0', '否', 1, 1),
    (4, NOW(), NOW(), 1, 1, 0, 0, 2, '1', '是', 2, 1),
    (5, NOW(), NOW(), 1, 1, 0, 0, 3, 'MENU', '菜单', 1, 1),
    (6, NOW(), NOW(), 1, 1, 0, 0, 3, 'BUTTON', '按钮', 2, 1),
    (7, NOW(), NOW(), 1, 1, 0, 0, 3, 'API', '接口', 3, 1),
    (8, NOW(), NOW(), 1, 1, 0, 0, 4, 'LOGIN', '登录日志', 1, 1),
    (9, NOW(), NOW(), 1, 1, 0, 0, 4, 'OPER', '操作日志', 2, 1),
    (10, NOW(), NOW(), 1, 1, 0, 0, 5, 'system', '系统公告', 1, 1),
    (11, NOW(), NOW(), 1, 1, 0, 0, 5, 'work', '工作通知', 2, 1),
    (12, NOW(), NOW(), 1, 1, 0, 0, 5, 'activity', '活动通知', 3, 1);

-- 演示公告用于全新环境验证全员与部门定向可见性。
INSERT INTO sys_notice
    (id, create_time, update_time, create_by, update_by, version, deleted,
     title, notice_type, content, scope_type, status, publish_time)
VALUES
    (1, NOW(), NOW(), 1, 1, 0, 0, '欢迎使用基础管理系统', 'system',
     '## 欢迎使用\n\n系统已完成初始化，请及时修改演示账号密码。', 'ALL', 'PUBLISHED', NOW()),
    (2, NOW(), NOW(), 1, 1, 0, 0, '本周工作安排', 'work',
     '## 工作安排\n\n请各部门按计划完成本周任务并及时更新进度。', 'ALL', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (3, NOW(), NOW(), 1, 1, 0, 0, '研发环境维护通知', 'system',
     '## 维护说明\n\n研发环境将进行例行维护，请提前保存工作。', 'TARGETED', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO sys_notice_target (notice_id, target_type, target_id)
VALUES (3, 'DEPT', 2);

INSERT INTO sys_config
    (id, create_time, update_time, create_by, update_by, version, deleted,
     config_key, config_value, remark)
VALUES
    (1, NOW(), NOW(), 1, 1, 0, 0, 'sys.title', '基础管理系统', '系统名称'),
    (2, NOW(), NOW(), 1, 1, 0, 0, 'sys.login.tokenExpireHours', '24', 'Token 有效期，单位小时'),
    (3, NOW(), NOW(), 1, 1, 0, 0, 'ai.deepseek.apiKey', NULL, 'DeepSeek API Key，运行时由管理员配置');

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'BasicProject 初始化完成，请立即修改演示账号默认密码 123456' AS message;
