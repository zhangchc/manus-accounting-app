package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_cost_history")
public class CostHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String stockCode;

    private String stockName;

    private Integer shares;

    private BigDecimal costPrice;

    private BigDecimal netInvestment;

    private BigDecimal currentPrice;

    private Long tradeRecordId;

    private String tradeType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
