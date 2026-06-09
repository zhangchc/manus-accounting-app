package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 预算请求DTO
 */
@Data
public class BudgetDTO {

    private Long categoryId;

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0", message = "预算金额不能为负数")
    private BigDecimal amount;

    @NotBlank(message = "年月不能为空")
    private String yearMonth;
}
