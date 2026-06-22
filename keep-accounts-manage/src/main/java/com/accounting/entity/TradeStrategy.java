package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_trade_strategy")
public class TradeStrategy {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String stockName;

    private String stockCode;

    private BigDecimal basePrice;

    private Integer sellShares;

    private Integer buyShares;

    private Integer maxSellCount;

    private Integer maxBuyCount;

    private Integer totalHolding;

    private BigDecimal alertWarningPrice;

    private BigDecimal alertCriticalPrice;

    private Integer sellCount;

    private Integer buyCount;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
