package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * AI确认入账请求DTO
 */
@Data
public class AiConfirmRecordDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    private String remark;

    private String recordDate;

    private String recordTime;
}
