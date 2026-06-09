package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiAccountingItemVO {
    private String categoryName;
    private BigDecimal amount;
}

