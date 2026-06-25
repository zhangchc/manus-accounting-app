package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeStrategyVO {

    private Long id;

    private String stockName;

    private String stockCode;

    private BigDecimal basePrice;

    private Integer sellShares;

    private Integer buyShares;

    private Integer maxSellCount;

    private Integer maxBuyCount;

    private BigDecimal alertWarningPrice;

    private BigDecimal alertCriticalPrice;

    private Integer sellCount;

    private Integer buyCount;

    private String status;

    private String updatedAt;
}
