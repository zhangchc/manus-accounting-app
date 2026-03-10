-- ============================================
-- 微信记账小程序 数据库初始化脚本
-- 数据库: MySQL 8.0
-- 字符集: utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS `accounting_app` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `accounting_app`;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `open_id` VARCHAR(64) NOT NULL COMMENT '微信openId',
  `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId',
  `nick_name` VARCHAR(64) DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(512) DEFAULT '' COMMENT '头像URL',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `monthly_budget` DECIMAL(12,2) DEFAULT 0.00 COMMENT '月预算',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 账本表
-- ============================================
DROP TABLE IF EXISTS `t_book`;
CREATE TABLE `t_book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账本ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(32) NOT NULL COMMENT '账本名称',
  `icon` VARCHAR(32) DEFAULT '📒' COMMENT '账本图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认账本 0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本表';

-- ============================================
-- 3. 分类表
-- ============================================
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `user_id` BIGINT DEFAULT 0 COMMENT '用户ID，0为系统预设',
  `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(32) NOT NULL COMMENT '分类图标',
  `type` TINYINT NOT NULL COMMENT '类型 1-支出 2-收入',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`user_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ============================================
-- 4. 记账记录表
-- ============================================
DROP TABLE IF EXISTS `t_record`;
CREATE TABLE `t_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '账本ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `type` TINYINT NOT NULL COMMENT '类型 1-支出 2-收入',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
  `remark` VARCHAR(200) DEFAULT '' COMMENT '备注',
  `record_date` DATE NOT NULL COMMENT '记账日期',
  `record_time` DATETIME NOT NULL COMMENT '记账时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `record_date`),
  KEY `idx_user_book` (`user_id`, `book_id`),
  KEY `idx_user_category` (`user_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账记录表';

-- ============================================
-- 5. 预算表
-- ============================================
DROP TABLE IF EXISTS `t_budget`;
CREATE TABLE `t_budget` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预算ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID，NULL为总预算',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '预算金额',
  `year_month` VARCHAR(7) NOT NULL COMMENT '年月 yyyy-MM',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_month` (`user_id`, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- ============================================
-- 初始化系统预设分类数据
-- ============================================

-- 支出分类
INSERT INTO `t_category` (`user_id`, `name`, `icon`, `type`, `sort_order`) VALUES
(0, '餐饮', '🍜', 1, 1),
(0, '交通', '🚗', 1, 2),
(0, '购物', '🛒', 1, 3),
(0, '日用', '🧴', 1, 4),
(0, '水果', '🍎', 1, 5),
(0, '零食', '🍪', 1, 6),
(0, '运动', '⚽', 1, 7),
(0, '娱乐', '🎮', 1, 8),
(0, '通讯', '📱', 1, 9),
(0, '服饰', '👔', 1, 10),
(0, '美容', '💄', 1, 11),
(0, '住房', '🏠', 1, 12),
(0, '居家', '🛋️', 1, 13),
(0, '孩子', '👶', 1, 14),
(0, '长辈', '👴', 1, 15),
(0, '社交', '🤝', 1, 16),
(0, '旅行', '✈️', 1, 17),
(0, '宠物', '🐱', 1, 18),
(0, '医疗', '💊', 1, 19),
(0, '学习', '📚', 1, 20),
(0, '其他', '📌', 1, 99);

-- 收入分类
INSERT INTO `t_category` (`user_id`, `name`, `icon`, `type`, `sort_order`) VALUES
(0, '工资', '💰', 2, 1),
(0, '奖金', '🎁', 2, 2),
(0, '兼职', '💼', 2, 3),
(0, '理财', '📈', 2, 4),
(0, '红包', '🧧', 2, 5),
(0, '转账', '💳', 2, 6),
(0, '退款', '↩️', 2, 7),
(0, '其他', '📌', 2, 99);
