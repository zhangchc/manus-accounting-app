package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 持仓概览汇总 VO，由服务层实时计算
 */
@Data
public class StockPositionSummaryVO {

    /** 总成本 = Σ(成本价 × 持仓股数) */
    private BigDecimal totalCost;

    /** 总市值 = Σ(现价 × 持仓股数)，现价来自行情接口 */
    private BigDecimal totalMarketValue;

    /** 总盈亏 = 总市值 − 总成本 */
    private BigDecimal totalProfit;

    /** 收益率 = (总盈亏 ÷ 总成本) × 100% */
    private BigDecimal totalProfitRate;

    /** 当日盈亏 = Σ(涨跌额 × 持仓股数) */
    private BigDecimal totalDailyPnl;

    /** 总仓位 = (总市值 ÷ 总成本) × 100% */
    private BigDecimal positionRate;
}
