package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 做T规则响应 VO，含交易统计
 */
@Data
public class StockTradeConfigVO {

    /** 规则ID */
    private Long id;

    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 基准价 */
    private BigDecimal basePrice;

    /** 档位数 */
    private Integer levels;

    /** 卖出每档涨幅 % */
    private BigDecimal upPct;

    /** 买入每档跌幅 % */
    private BigDecimal downPct;

    /** 每档操作固定股数 */
    private Integer fixedShares;

    /** 状态 */
    private Integer active;

    /** 累计卖出次数 */
    private Integer sellCount;

    /** 累计买入次数 */
    private Integer buyCount;

    /** 总盈亏 = 卖出总额 - 买入总额 */
    private BigDecimal totalPnl;
}
