package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeRecordVO {

    private Long id;

    private Long strategyId;

    private String stockName;

    private String stockCode;

    private String tradeType;

    private BigDecimal tradePrice;

    private Integer shares;

    private Integer sellNo;

    private Integer buyNo;

    private String opLevel;

    private BigDecimal backBuyPrice;

    private Long matchedSellId;

    private BigDecimal matchedSellPrice;

    private BigDecimal profit;

    private Integer currentHolding;

    private String scenario;

    private String reason;

    private String remark;

    private String createdAt;
}
