-- ============================================
-- 股票做T管理 - 数据库初始化
-- ============================================
USE `accounting`;

-- ============================================
-- 1. 做T策略配置表
-- ============================================
DROP TABLE IF EXISTS `t_trade_strategy`;
CREATE TABLE `t_trade_strategy` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `stock_name` VARCHAR(50) NOT NULL DEFAULT '常山药业' COMMENT '股票名称',
  `stock_code` VARCHAR(10) NOT NULL DEFAULT 'sz300255' COMMENT '股票代码',
  `base_price` DECIMAL(10,4) NOT NULL COMMENT '基准价',
  `sell_shares` INT NOT NULL DEFAULT 600 COMMENT '每次卖出股数',
  `buy_shares` INT NOT NULL DEFAULT 600 COMMENT '每次买入股数',
  `max_sell_count` INT NOT NULL DEFAULT 3 COMMENT '最多卖出次数',
  `max_buy_count` INT NOT NULL DEFAULT 3 COMMENT '最多买入次数',
  `total_holding` INT NOT NULL DEFAULT 6800 COMMENT '初始总持仓',
  `alert_warning_price` DECIMAL(10,4) DEFAULT 25 COMMENT '黄色预警价',
  `alert_critical_price` DECIMAL(10,4) DEFAULT 22 COMMENT '红色紧急价',
  `sell_count` INT NOT NULL DEFAULT 0 COMMENT '当前累计卖出次数',
  `buy_count` INT NOT NULL DEFAULT 0 COMMENT '当前累计买入次数',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/PAUSED',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='做T策略配置表';

-- ============================================
-- 2. 做T操作记录表
-- ============================================
DROP TABLE IF EXISTS `t_trade_record`;
CREATE TABLE `t_trade_record` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `strategy_id` BIGINT NOT NULL COMMENT '策略ID',
  `stock_name` VARCHAR(50) NOT NULL COMMENT '股票名称',
  `stock_code` VARCHAR(10) NOT NULL COMMENT '股票代码',
  `trade_type` VARCHAR(10) NOT NULL COMMENT 'SELL卖出/BUY买入',
  `trade_price` DECIMAL(10,4) NOT NULL COMMENT '成交价',
  `shares` INT NOT NULL COMMENT '成交股数',
  `sell_no` INT DEFAULT NULL COMMENT '第几次卖出(仅SELL)',
  `buy_no` INT DEFAULT NULL COMMENT '第几次买入(仅BUY)',
  `op_level` VARCHAR(20) DEFAULT NULL COMMENT '操作级别: NORMAL/BOUNDARY/OVERLIMIT',
  `back_buy_price` DECIMAL(10,4) DEFAULT NULL COMMENT '目标回补价(仅SELL)',
  `matched_sell_id` BIGINT DEFAULT NULL COMMENT '配对的卖出记录ID(仅BUY)',
  `profit` DECIMAL(12,2) DEFAULT NULL COMMENT '本次获利(仅BUY)',
  `current_holding` INT DEFAULT NULL COMMENT '操作后持仓',
  `reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '操作理由',
  `scenario` VARCHAR(100) DEFAULT '' COMMENT '操作场景: 计划内卖出/末次卖出-上涨中/超限买入-下跌中等',
  `remark` VARCHAR(200) DEFAULT '' COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id` (`strategy_id`),
  KEY `idx_trade_type` (`trade_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='做T操作记录表';

-- ============================================
-- 3. 菜单结构调整（幂等，可重复执行）
-- 创建一级目录"股票管理"，其下两个子菜单："股票持仓"和"做T管理"
-- ============================================

-- 3.1 确保一级目录"股票管理"存在 (INSERT or UPDATE)
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `icon`, `type`, `path`, `component`, `permission`, `sort`, `status`, `created_user`, `updated_user`, `created_at`, `updated_at`, `deleted`)
VALUES (1610000000000000001, 0, '股票管理', 'TrendCharts', 'dir', '/stock-mgr', '', '', 99, 1, 1, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
    `parent_id` = 0,
    `name` = '股票管理',
    `icon` = 'TrendCharts',
    `type` = 'dir',
    `path` = '/stock-mgr',
    `component` = '',
    `permission` = '',
    `sort` = 99,
    `status` = 1,
    `updated_at` = NOW();

-- 3.2 创建子菜单"股票持仓" (id=1610000000000000002)
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `icon`, `type`, `path`, `component`, `permission`, `sort`, `status`, `created_user`, `updated_user`, `created_at`, `updated_at`, `deleted`)
VALUES (1610000000000000002, 1610000000000000001, '股票持仓', 'PieChart', 'menu', '/stock', 'views/investment/stock/index', 'stock:view', 1, 1, 1, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
    `parent_id` = 1610000000000000001,
    `name` = '股票持仓',
    `icon` = 'PieChart',
    `type` = 'menu',
    `path` = '/stock',
    `component` = 'views/investment/stock/index',
    `permission` = 'stock:view',
    `sort` = 1,
    `status` = 1,
    `updated_at` = NOW();

-- 3.3 创建子菜单"做T管理" (id=1610000000000000003)
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `icon`, `type`, `path`, `component`, `permission`, `sort`, `status`, `created_user`, `updated_user`, `created_at`, `updated_at`, `deleted`)
VALUES (1610000000000000003, 1610000000000000001, '做T管理', 'DataLine', 'menu', '/trade', 'views/investment/trade/index', 'trade:view', 2, 1, 1, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
    `parent_id` = 1610000000000000001,
    `name` = '做T管理',
    `icon` = 'DataLine',
    `type` = 'menu',
    `path` = '/trade',
    `component` = 'views/investment/trade/index',
    `permission` = 'trade:view',
    `sort` = 2,
    `status` = 1,
    `updated_at` = NOW();

-- 3.4 确保一级目录也分配给角色（否则子菜单可能孤立）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.role_id, 1610000000000000001
FROM `sys_role_menu` rm
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm2
    WHERE rm2.role_id = rm.role_id AND rm2.menu_id = 1610000000000000001
);

-- 3.5 将子菜单分配给已有角色
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.role_id, 1610000000000000002
FROM `sys_role_menu` rm
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm2
    WHERE rm2.role_id = rm.role_id AND rm2.menu_id = 1610000000000000002
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.role_id, 1610000000000000003
FROM `sys_role_menu` rm
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm2
    WHERE rm2.role_id = rm.role_id AND rm2.menu_id = 1610000000000000003
);

-- ============================================
-- 4. 新增 scenario 字段（增量迁移）
-- ============================================
-- 增量迁移：已建表执行此句添加 scenario 字段（可重复执行）
ALTER TABLE `t_trade_record`
  ADD COLUMN IF NOT EXISTS `scenario` VARCHAR(100) DEFAULT '' COMMENT '操作场景' AFTER `reason`;
