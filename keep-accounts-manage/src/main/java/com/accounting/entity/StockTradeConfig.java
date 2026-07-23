package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 做T管理规则实体
 */
@Data
@TableName("t_stock_config")
public class StockTradeConfig {

    /** 主键ID（雪花ID） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 基准价（网格中枢） */
    private BigDecimal basePrice;

    /** 档位数（买入/卖出各N档） */
    private Integer levels;

    /** 卖出每档涨幅 % */
    private BigDecimal upPct;

    /** 买入每档跌幅 % */
    private BigDecimal downPct;

    /** 每档操作固定股数 */
    private Integer fixedShares;

    /** 状态 0-停用 1-启用 */
    private Integer active;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;
}
