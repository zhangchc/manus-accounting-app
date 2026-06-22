package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockPositionVO {

    private Long id;

    private String stockName;

    private String stockCode;

    private BigDecimal costPrice;

    private Integer shares;

    private BigDecimal totalCost;

    private BigDecimal currentPrice;

    private BigDecimal currentValue;

    private BigDecimal profitLoss;

    private BigDecimal profitLossPercent;

    private BigDecimal dailyProfitLoss;

    private BigDecimal change;

    private String changePercent;

    private String updatedAt;
}
