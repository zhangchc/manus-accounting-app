package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 记账记录VO
 */
@Data
public class RecordVO {

    private Long id;
    private Long bookId;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private Integer type;
    private BigDecimal amount;
    private String remark;
    private String recordDate;
    private String recordTime;
    private String createTime;
}
