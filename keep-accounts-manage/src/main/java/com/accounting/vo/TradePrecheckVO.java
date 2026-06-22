package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TradePrecheckVO {

    private String opLevel;

    private Integer nextSellNo;

    private Integer nextBuyNo;

    private String warning;

    private Boolean reasonRequired;

    private Integer currentHolding;

    private Integer holdingAfterOp;

    private String scenario;

    private List<String> suggestedReasons;
}
