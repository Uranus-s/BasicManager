CREATE TABLE sys_user
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',

    version     INT     DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

    username    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(128) NOT NULL COMMENT '登录密码',
    nickname    VARCHAR(64) COMMENT '昵称',
    phone       VARCHAR(20) COMMENT '手机号',
    email       VARCHAR(100) COMMENT '邮箱',
    avatar      VARCHAR(255) COMMENT '头像',
    status      TINYINT DEFAULT 1 COMMENT '状态 0=禁用 1=正常'
) COMMENT='系统用户表';

CREATE TABLE sys_role
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',

    version     INT     DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

    role_code   VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name   VARCHAR(64) NOT NULL COMMENT '角色名称',
    status      TINYINT DEFAULT 1 COMMENT '状态 0=禁用 1=正常',
    remark      VARCHAR(255) COMMENT '备注'
) COMMENT='系统角色表';

CREATE TABLE sys_permission
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',

    version     INT     DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

    parent_id   BIGINT COMMENT '父ID',
    name        VARCHAR(64) NOT NULL COMMENT '名称',
    type        VARCHAR(20) COMMENT '类型 MENU/BUTTON/API',
    path        VARCHAR(255) COMMENT '路由路径或接口路径',
    component   VARCHAR(255) COMMENT '前端组件路径',
    permission  VARCHAR(100) COMMENT '权限标识 sys:user:add',
    icon        VARCHAR(100) COMMENT '图标',
    sort        INT     DEFAULT 0 COMMENT '排序',
    visible     TINYINT DEFAULT 1 COMMENT '是否显示 0=隐藏 1=显示',
    status      TINYINT DEFAULT 1 COMMENT '状态 0=禁用 1=正常'
) COMMENT='菜单权限表';

CREATE TABLE sys_user_role
(
    user_id BIGINT COMMENT '用户ID',
    role_id BIGINT COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) COMMENT='用户角色关联表';

CREATE TABLE sys_role_permission
(
    role_id       BIGINT COMMENT '角色ID',
    permission_id BIGINT COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id)
) COMMENT='角色权限关联表';

CREATE TABLE sys_login_log
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',

    username    VARCHAR(64) COMMENT '用户名',
    ip          VARCHAR(64) COMMENT 'IP地址',
    browser     VARCHAR(100) COMMENT '浏览器',
    os          VARCHAR(100) COMMENT '操作系统',
    status      TINYINT COMMENT '状态 0=失败 1=成功',
    msg         VARCHAR(255) COMMENT '提示消息'
) COMMENT='登录日志表';

CREATE TABLE sys_oper_log
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time     DATETIME COMMENT '创建时间',

    module          VARCHAR(100) COMMENT '模块名',
    method          VARCHAR(100) COMMENT '方法名',
    request_url     VARCHAR(255) COMMENT '请求URL',
    request_method  VARCHAR(20) COMMENT '请求方式',
    request_params  TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '返回结果',
    status          TINYINT COMMENT '状态 0=失败 1=成功',
    cost_time       BIGINT COMMENT '耗时(ms)'
) COMMENT='操作日志表';

CREATE TABLE sys_dict
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',

    version     INT     DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除 0=未删除 1=已删除',

    dict_code   VARCHAR(100) NOT NULL COMMENT '字典编码',
    dict_name   VARCHAR(100) NOT NULL COMMENT '字典名称',
    status      TINYINT DEFAULT 1 COMMENT '状态'
) COMMENT='系统字典表';

CREATE TABLE sys_dict_item
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',

    version     INT     DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除',

    dict_id     BIGINT COMMENT '字典ID',
    item_value  VARCHAR(100) COMMENT '值',
    item_label  VARCHAR(100) COMMENT '标签',
    sort        INT     DEFAULT 0 COMMENT '排序',
    status      TINYINT DEFAULT 1 COMMENT '状态'
) COMMENT='字典项表';

CREATE TABLE sys_config
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time  DATETIME COMMENT '创建时间',
    update_time  DATETIME COMMENT '更新时间',
    create_by    BIGINT COMMENT '创建人',
    update_by    BIGINT COMMENT '更新人',

    version      INT     DEFAULT 0 COMMENT '乐观锁版本号',
    deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除',

    config_key   VARCHAR(100) COMMENT '参数键',
    config_value VARCHAR(255) COMMENT '参数值',
    remark       VARCHAR(255) COMMENT '备注'
) COMMENT='系统参数表';

CREATE TABLE sys_dept
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_by   BIGINT COMMENT '创建人',
    update_by   BIGINT COMMENT '更新人',

    version     INT     DEFAULT 0 COMMENT '乐观锁版本号',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除',

    parent_id   BIGINT COMMENT '父部门ID',
    dept_name   VARCHAR(100) COMMENT '部门名称',
    leader      VARCHAR(50) COMMENT '负责人',
    phone       VARCHAR(20) COMMENT '联系电话',
    sort        INT     DEFAULT 0 COMMENT '排序'
) COMMENT='部门表';

CREATE TABLE sys_user_dept
(
    user_id BIGINT COMMENT '用户ID',
    dept_id BIGINT COMMENT '部门ID',
    PRIMARY KEY (user_id, dept_id)
) COMMENT='用户部门关联表';

CREATE TABLE sys_file
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',

    create_time DATETIME COMMENT '创建时间',
    create_by   BIGINT COMMENT '创建人',

    file_name   VARCHAR(255) COMMENT '文件名',
    file_path   VARCHAR(500) COMMENT '文件路径',
    file_size   BIGINT COMMENT '文件大小',
    file_type   VARCHAR(50) COMMENT '文件类型',
    biz_type    VARCHAR(50) COMMENT '业务类型'
) COMMENT='文件表';

