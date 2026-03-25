package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI解析草稿
 */
@Data
public class AiDraftVO {
    private Integer type;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private String remark;
    private String recordDate;
    private String recordTime;
    private Double confidence;
}
