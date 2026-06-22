-- ============================================
-- 股票持仓表
-- ============================================
USE `accounting`;

DROP TABLE IF EXISTS `t_stock_position`;
CREATE TABLE `t_stock_position` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `stock_name` VARCHAR(50) NOT NULL COMMENT '股票名称',
  `stock_code` VARCHAR(10) NOT NULL COMMENT '股票代码',
  `cost_price` DECIMAL(10,4) NOT NULL COMMENT '成本价(元/股)',
  `shares` INT NOT NULL COMMENT '持有股数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='股票持仓表';

-- ============================================
-- 新增"股票持仓"一级菜单（如果尚未存在）
-- 注意：stock_trade.sql 已将 1610000000000000001 改为目录"股票管理"，
--       其下子菜单 1610000000000000002/3 分别指向股票持仓和做T管理
-- ============================================
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `icon`, `type`, `path`, `component`, `permission`, `sort`, `status`, `created_user`, `updated_user`, `created_at`, `updated_at`, `deleted`)
SELECT 1610000000000000001, 0, '股票持仓', 'TrendCharts', 'menu', '/stock', 'investment/stock/index', 'stock:view', 99, 1, 1, 1, NOW(), NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `id` = 1610000000000000001
);

-- 将菜单分配给所有已有菜单的角色（自动适配实际管理员角色）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.role_id, 1610000000000000001
FROM `sys_role_menu` rm
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm2
    WHERE rm2.role_id = rm.role_id AND rm2.menu_id = 1610000000000000001
);
