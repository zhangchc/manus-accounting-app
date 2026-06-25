package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CostHistoryVO {

    private Long id;
    // 交易后持股数
    private Integer shares;
    // 交易后回本价
    private BigDecimal costPrice;
    // 交易后累计净投入
    private BigDecimal netInvestment;
    // 交易时最新价
    private BigDecimal currentPrice;
    // 交易类型 SELL / BUY
    private String tradeType;
    // 创建时间
    private String createdAt;
}
