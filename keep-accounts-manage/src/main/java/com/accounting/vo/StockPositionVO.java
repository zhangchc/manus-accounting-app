package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 股票持仓响应 VO，含实时行情计算字段
 */
@Data
public class StockPositionVO {

    /** 持仓 ID */
    private Long id;

    /** 股票代码 */
    private String stockCode;

    /** 股票名称 */
    private String stockName;

    /** 持仓股数 */
    private Integer shares;

    /** 成本价（元） */
    private BigDecimal costPrice;

    /** 现价（元），行情接口获取，不落库 */
    private BigDecimal currentPrice;

    /** 市值 = 现价 × 持股数，实时计算 */
    private BigDecimal marketValue;

    /** 盈亏金额 = (现价 − 成本价) × 持股数 */
    private BigDecimal profit;

    /** 盈亏比例 = (现价 − 成本价) ÷ 成本价 × 100% */
    private BigDecimal profitRate;
}
