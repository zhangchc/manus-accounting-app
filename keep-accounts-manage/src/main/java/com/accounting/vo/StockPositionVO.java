package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockPositionVO {

    private Long id;
    // 股票名称
    private String stockName;
    // 股票代码（如 sz300255）
    private String stockCode;
    // 持仓成本价（元/股）
    private BigDecimal costPrice;
    // 持股数（股）
    private Integer shares;
    // 累计净投入（买入追加-卖出回收），用于摊薄法计算回本价
    private BigDecimal netInvestment;
    // 持仓总成本 = 成本价 × 持股数（前端「持仓成本」卡片）
    private BigDecimal totalCost;

    // 股票最新成交价（前端「实时行情」最新价）
    private BigDecimal currentPrice;

    // 当前市值 = 最新价 × 持股数（前端「当前市值」卡片）
    private BigDecimal currentValue;

    // 浮动盈亏 = (最新价 - 成本价) × 持股数（前端「浮动盈亏」卡片）
    private BigDecimal profitLoss;

    // 浮动盈亏比例 = (最新价 - 成本价) / 成本价 × 100%（前端「盈亏比例」卡片）
    private BigDecimal profitLossPercent;

    // 当日盈亏 = (最新价 - 昨收) × 持股数（前端「当日盈亏」卡片）
    private BigDecimal dailyProfitLoss;

    // 今日涨跌额（元）
    private BigDecimal change;

    // 今日涨跌幅（%）
    private String changePercent;

    // 数据更新时间
    private String updatedAt;
}
