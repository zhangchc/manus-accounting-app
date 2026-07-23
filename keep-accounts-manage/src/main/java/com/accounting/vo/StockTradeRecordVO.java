package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 做T交易流水 VO，含实时计算的配对盈亏
 */
@Data
public class StockTradeRecordVO {

    /** 流水ID */
    private Long id;

    /** 关联规则ID */
    private Long configId;

    /** 关联档位ID */
    private Long operationId;

    /** 股票代码 */
    private String stockCode;

    /** 买卖方向 1-买入 2-卖出 */
    private Integer direction;

    /** 操作股数 */
    private Integer shares;

    /** 成交价 */
    private BigDecimal price;

    /** 买卖理由 */
    private String reason;

    /** 交易时间 */
    private LocalDateTime tradeTime;

    /**
     * 配对盈亏（FIFO 实时计算）
     * null 表示待匹配，非 null 为具体盈亏金额
     */
    private BigDecimal pairProfit;
}
