package com.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecordQueryDTO {

    private String userNickName;

    private Long categoryId;

    private Integer type;

    private String startDate;

    private String endDate;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private Integer page = 1;

    private Integer pageSize = 10;
}
