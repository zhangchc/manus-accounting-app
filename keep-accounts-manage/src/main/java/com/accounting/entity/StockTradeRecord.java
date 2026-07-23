package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 做T流水实体
 */
@Data
@TableName("t_stock_trade")
public class StockTradeRecord {

    /** 主键ID（雪花ID） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联规则表 t_stock_config.id */
    private Long configId;

    /** 关联档位表 t_stock_operation.id（满档强制卖出时为 null） */
    private Long operationId;

    /** 股票代码（冗余，方便查询） */
    private String stockCode;

    /** 买卖方向 1-买入 2-卖出 */
    private Integer direction;

    /** 操作股数 */
    private Integer shares;

    /** 成交价（元） */
    private BigDecimal price;

    /** 买卖理由 */
    private String reason;

    /** 交易时间 */
    private LocalDateTime tradeTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
