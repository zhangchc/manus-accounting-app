/*
 Navicat Premium Data Transfer

 Source Server         : 8.153.192.39
 Source Server Type    : MySQL
 Source Server Version : 80045
 Source Host           : 8.153.192.39:3306
 Source Schema         : accounting_app

 Target Server Type    : MySQL
 Target Server Version : 80045
 File Encoding         : 65001

 Date: 22/06/2026 16:57:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Migration: 添加 user_id 字段实现用户数据隔离
-- 如果表已存在，执行以下 ALTER TABLE 语句
-- ----------------------------
-- ALTER TABLE t_stock_position ADD COLUMN `user_id` bigint NOT NULL COMMENT '用户ID' AFTER `id`, ADD INDEX `idx_user_id` (`user_id`);
-- ALTER TABLE t_trade_record ADD COLUMN `user_id` bigint NOT NULL COMMENT '用户ID' AFTER `id`, ADD INDEX `idx_user_id` (`user_id`);
-- ALTER TABLE t_trade_strategy ADD COLUMN `user_id` bigint NOT NULL COMMENT '用户ID' AFTER `id`, ADD INDEX `idx_user_id` (`user_id`);
--
-- Migration: 摊薄法联动 - 新增 net_investment 字段和成本历史表
-- ALTER TABLE t_stock_position ADD COLUMN `net_investment` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '累计净投入' AFTER `shares`;
-- UPDATE t_stock_position SET net_investment = cost_price * shares WHERE net_investment = 0;

-- ----------------------------
-- Table structure for t_stock_position
-- ----------------------------
DROP TABLE IF EXISTS `t_stock_position`;
CREATE TABLE `t_stock_position` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stock_name` varchar(50) NOT NULL COMMENT '股票名称',
  `stock_code` varchar(10) NOT NULL COMMENT '股票代码',
  `cost_price` decimal(10,4) NOT NULL COMMENT '成本价(元/股)',
  `shares` int NOT NULL COMMENT '持有股数',
  `net_investment` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '累计净投入(买入追加-卖出回收)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股票持仓表';

-- ----------------------------
-- Table structure for t_cost_history
-- ----------------------------
DROP TABLE IF EXISTS `t_cost_history`;
CREATE TABLE `t_cost_history` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stock_code` varchar(10) NOT NULL COMMENT '股票代码',
  `stock_name` varchar(50) DEFAULT NULL COMMENT '股票名称',
  `shares` int NOT NULL COMMENT '交易后持股数',
  `cost_price` decimal(10,4) NOT NULL COMMENT '交易后回本价',
  `net_investment` decimal(14,2) NOT NULL COMMENT '交易后累计净投入',
  `current_price` decimal(10,4) DEFAULT NULL COMMENT '交易时最新价',
  `trade_record_id` bigint DEFAULT NULL COMMENT '关联交易记录ID',
  `trade_type` varchar(10) DEFAULT NULL COMMENT 'SELL/BUY',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='持仓成本历史记录表';

-- ----------------------------
-- Records of t_stock_position
-- ----------------------------
BEGIN;
INSERT INTO `t_stock_position` VALUES (2067973907700310018, '常山药业', 'sz300255', 64.0000, 6800, '2026-06-19 22:12:55', '2026-06-19 22:12:55', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_trade_record
-- ----------------------------
DROP TABLE IF EXISTS `t_trade_record`;
CREATE TABLE `t_trade_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `strategy_id` bigint NOT NULL COMMENT '策略ID',
  `stock_name` varchar(50) NOT NULL COMMENT '股票名称',
  `stock_code` varchar(10) NOT NULL COMMENT '股票代码',
  `trade_type` varchar(10) NOT NULL COMMENT 'SELL卖出/BUY买入',
  `trade_price` decimal(10,4) NOT NULL COMMENT '成交价',
  `shares` int NOT NULL COMMENT '成交股数',
  `sell_no` int DEFAULT NULL COMMENT '第几次卖出(仅SELL)',
  `buy_no` int DEFAULT NULL COMMENT '第几次买入(仅BUY)',
  `op_level` varchar(20) DEFAULT NULL COMMENT '操作级别: NORMAL/BOUNDARY/OVERLIMIT',
  `back_buy_price` decimal(10,4) DEFAULT NULL COMMENT '目标回补价(仅SELL)',
  `matched_sell_id` bigint DEFAULT NULL COMMENT '配对的卖出记录ID(仅BUY)',
  `profit` decimal(12,2) DEFAULT NULL COMMENT '本次获利(仅BUY)',
  `current_holding` int DEFAULT NULL COMMENT '操作后持仓',
  `reason` varchar(500) NOT NULL DEFAULT '' COMMENT '操作理由',
  `scenario` varchar(100) DEFAULT '' COMMENT '操作场景: 计划内卖出/末次卖出-上涨中/超限买入-下跌中等',
  `remark` varchar(200) DEFAULT '' COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_strategy_id` (`strategy_id`),
  KEY `idx_trade_type` (`trade_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='做T操作记录表';

-- ----------------------------
-- Records of t_trade_record
-- ----------------------------
BEGIN;
INSERT INTO `t_trade_record` VALUES (2068968080088723457, 2067980096269983746, '常山药业', 'sz300255', 'SELL', 25.8300, 600, 5, NULL, 'NORMAL', 24.6000, NULL, NULL, 6200, '今日常山跌了6个多点，全天一直绿，我看下午要红了，挂红了的点位卖单600', '计划内卖出-上涨中', '', '2026-06-22 16:03:25', 0);
COMMIT;

-- ----------------------------
-- Table structure for t_trade_strategy
-- ----------------------------
DROP TABLE IF EXISTS `t_trade_strategy`;
CREATE TABLE `t_trade_strategy` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stock_name` varchar(50) NOT NULL DEFAULT '常山药业' COMMENT '股票名称',
  `stock_code` varchar(10) NOT NULL DEFAULT 'sz300255' COMMENT '股票代码',
  `base_price` decimal(10,4) NOT NULL COMMENT '基准价',
  `sell_shares` int NOT NULL DEFAULT '600' COMMENT '每次卖出股数',
  `buy_shares` int NOT NULL DEFAULT '600' COMMENT '每次买入股数',
  `max_sell_count` int NOT NULL DEFAULT '3' COMMENT '最多卖出次数',
  `max_buy_count` int NOT NULL DEFAULT '3' COMMENT '最多买入次数',
  `total_holding` int NOT NULL DEFAULT '6800' COMMENT '初始总持仓',
  `alert_warning_price` decimal(10,4) DEFAULT '25.0000' COMMENT '黄色预警价',
  `alert_critical_price` decimal(10,4) DEFAULT '22.0000' COMMENT '红色紧急价',
  `sell_count` int NOT NULL DEFAULT '0' COMMENT '当前累计卖出次数',
  `buy_count` int NOT NULL DEFAULT '0' COMMENT '当前累计买入次数',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/PAUSED',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='做T策略配置表';

-- ----------------------------
-- Records of t_trade_strategy
-- ----------------------------
BEGIN;
INSERT INTO `t_trade_strategy` VALUES (2067980096269983746, '常山药业', 'sz300255', 24.6000, 1000, 1000, 3, 3, 6800, 22.0000, 20.0000, 5, 3, 'ACTIVE', '2026-06-19 22:37:31', '2026-06-19 22:37:31', 0);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
