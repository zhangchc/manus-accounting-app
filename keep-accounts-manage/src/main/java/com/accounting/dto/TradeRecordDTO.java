package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TradeRecordDTO {

    @NotNull(message = "成交价不能为空")
    @DecimalMin(value = "0.01", message = "成交价必须大于0")
    private BigDecimal tradePrice;

    @NotNull(message = "股数不能为空")
    @Min(value = 1, message = "股数必须大于0")
    private Integer shares;

    private String reason;

    private String remark;
}
