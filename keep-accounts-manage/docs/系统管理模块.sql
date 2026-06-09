-- ============================================================
-- 1. sys_user 管理员表
-- ============================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(64)   NOT NULL COMMENT '用户名',
    password    VARCHAR(128)  NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(64)   NOT NULL COMMENT '昵称',
    email       VARCHAR(128)  DEFAULT '' COMMENT '邮箱',
    phone       VARCHAR(20)   DEFAULT '' COMMENT '手机号',
    status      TINYINT       DEFAULT 1 COMMENT '状态：1启用 0禁用',
    last_login  DATETIME      DEFAULT NULL COMMENT '上次登录时间',
    created_user          BIGINT        COMMENT '创建人用户id',
    updated_user          BIGINT        COMMENT '修改人用户id',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       DEFAULT 0 COMMENT '逻辑删除：1已删除 0未删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================================
-- 2. sys_role 角色表
-- ============================================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(64)   NOT NULL COMMENT '角色名称',
    code        VARCHAR(64)   NOT NULL COMMENT '角色编码',
    `desc`      VARCHAR(255)  DEFAULT '' COMMENT '描述',
    sort        INT           DEFAULT 0 COMMENT '排序',
    status      TINYINT       DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_user          BIGINT        COMMENT '创建人用户id',
    updated_user          BIGINT        COMMENT '修改人用户id',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       DEFAULT 0 COMMENT '逻辑删除：1已删除 0未删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================================
-- 3. sys_user_role 用户角色关联表
-- ============================================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id          BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT  NOT NULL COMMENT '用户ID',
    role_id     BIGINT  NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================================
-- 4. sys_menu 菜单权限表
-- ============================================================
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    parent_id   BIGINT        DEFAULT 0 COMMENT '父级ID，0=顶级',
    name        VARCHAR(64)   NOT NULL COMMENT '名称',
    icon        VARCHAR(64)   DEFAULT '' COMMENT '图标（Element Plus图标名）',
    type        VARCHAR(16)   NOT NULL COMMENT '类型：dir=目录 menu=菜单 btn=按钮',
    path        VARCHAR(128)  DEFAULT '' COMMENT '路由路径',
    component   VARCHAR(128)  DEFAULT '' COMMENT '组件路径',
    permission  VARCHAR(128)  DEFAULT '' COMMENT '权限标识',
    sort        INT           DEFAULT 0 COMMENT '排序',
    status      TINYINT       DEFAULT 1 COMMENT '状态：1启用 0禁用',
    created_user          BIGINT        COMMENT '创建人用户id',
    updated_user          BIGINT        COMMENT '修改人用户id',
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       DEFAULT 0 COMMENT '逻辑删除：1已删除 0未删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ============================================================
-- 5. sys_role_menu 角色菜单关联表
-- ============================================================
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id          BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id     BIGINT  NOT NULL COMMENT '角色ID',
    menu_id     BIGINT  NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================================
-- 菜单初始化数据（15条，3层树形，与前端 mockMenus 完全一致）
-- ============================================================
INSERT INTO sys_menu (id, parent_id, name, icon, type, path, component, permission, sort, status) VALUES
-- 系统管理（目录）
(1,   0, '系统管理',   'Settings',   'dir',  '/system',          '',                             '',                1, 1),
(11,  1, '管理员管理', 'Users',      'menu', '/system/admins',   'views/system/admins/index',    '',                1, 1),
(111, 11,'新增管理员', '',           'btn',  '',                  '',                             'sys:admin:create', 1, 1),
(112, 11,'编辑管理员', '',           'btn',  '',                  '',                             'sys:admin:edit',   2, 1),
(113, 11,'删除管理员', '',           'btn',  '',                  '',                             'sys:admin:delete', 3, 1),
(12,  1, '角色管理',   'Shield',     'menu', '/system/roles',    'views/system/roles/index',     '',                2, 1),
(121, 12,'新增角色',   '',           'btn',  '',                  '',                             'sys:role:create',  1, 1),
(122, 12,'分配权限',   '',           'btn',  '',                  '',                             'sys:role:assign',  2, 1),
(13,  1, '菜单管理',   'Menu',       'menu', '/system/menus',    'views/system/menus/index',     '',                3, 1),
(131, 13,'新增菜单',   '',           'btn',  '',                  '',                             'sys:menu:create',  1, 1),
-- 应用管理（目录）
(2,   0, '应用管理',   'Smartphone', 'dir',  '/app',             '',                             '',                2, 1),
(21,  2, '小程序用户', 'Smartphone', 'menu', '/app/users',       'views/app/users/index',        '',                1, 1),
(22,  2, '记账记录',   'BookOpen',   'menu', '/app/records',     'views/app/records/index',      '',                2, 1),
(23,  2, '分类管理',   'Tag',        'menu', '/app/categories',  'views/app/categories/index',   '',                3, 1),
-- 操作日志（顶级菜单）
(3,   0, '操作日志',   'FileText',   'menu', '/logs',            'views/logs/index',             '',                3, 1);
