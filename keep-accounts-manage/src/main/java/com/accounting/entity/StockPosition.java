package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_stock_position")
public class StockPosition {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String stockName;

    private String stockCode;

    private BigDecimal costPrice;

    private Integer shares;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
