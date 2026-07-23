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
 * 股价到达告警记录表，用于防重复推送
 */
@Data
@TableName("t_stock_price_alert")
public class StockPriceAlert {

    /** 主键ID（雪花ID） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联 t_stock_config.id */
    private Long configId;

    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 买卖方向 1-买入 2-卖出 */
    private Integer direction;

    /** 档位编号 */
    private Integer levelNo;

    /** 目标触发价 */
    private BigDecimal triggerPrice;

    /** 通知时的现价 */
    private BigDecimal currentPrice;

    /** 使用的 SendKey */
    private String sendKey;

    /** 通知时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
