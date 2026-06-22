package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class StockPositionDTO {

    @NotBlank(message = "股票名称不能为空")
    private String stockName;

    @NotBlank(message = "股票代码不能为空")
    private String stockCode;

    @DecimalMin(value = "0.01", message = "成本价必须大于0")
    private BigDecimal costPrice;

    @Min(value = 1, message = "股数必须大于0")
    private Integer shares;
}
