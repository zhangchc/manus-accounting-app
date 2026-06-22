package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_trade_record")
public class TradeRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long strategyId;

    private String stockName;

    private String stockCode;

    private String tradeType;

    private BigDecimal tradePrice;

    private Integer shares;

    private Integer sellNo;

    private Integer buyNo;

    private String opLevel;

    private BigDecimal backBuyPrice;

    private Long matchedSellId;

    private BigDecimal profit;

    private Integer currentHolding;

    private String scenario;

    private String reason;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
