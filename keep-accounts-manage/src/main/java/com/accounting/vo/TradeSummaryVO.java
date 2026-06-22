package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class TradeSummaryVO {

    private TradeStrategyVO strategy;

    private BigDecimal currentPrice;

    private BigDecimal changeAmount;

    private String changePercent;

    private List<Map<String, Object>> sellLevels;

    private List<Map<String, Object>> buyLevels;

    private List<Map<String, Object>> alerts;

    private BigDecimal totalProfit;

    private Integer unmatchedSellCount;

    private Integer unmatchedBuyCount;

    private Integer totalSellCount;

    private Integer totalSellShares;

    private Integer totalBuyCount;

    private Integer totalBuyShares;

    private Integer currentHolding;

    private BigDecimal currentMarketValue;
}
