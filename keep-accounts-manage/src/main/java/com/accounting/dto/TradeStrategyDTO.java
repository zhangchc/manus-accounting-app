package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TradeStrategyDTO {

    @NotNull(message = "基准价不能为空")
    @DecimalMin(value = "0.01", message = "基准价必须大于0")
    private BigDecimal basePrice;

    @NotNull(message = "卖出股数不能为空")
    @Min(value = 1, message = "卖出股数必须大于0")
    private Integer sellShares;

    @NotNull(message = "买入股数不能为空")
    @Min(value = 1, message = "买入股数必须大于0")
    private Integer buyShares;

    @NotNull(message = "最多卖出次数不能为空")
    @Min(value = 1, message = "最多卖出次数必须大于0")
    private Integer maxSellCount;

    @NotNull(message = "最多买入次数不能为空")
    @Min(value = 1, message = "最多买入次数必须大于0")
    private Integer maxBuyCount;

    @NotNull(message = "总持仓不能为空")
    @Min(value = 1, message = "总持仓必须大于0")
    private Integer totalHolding;

    private BigDecimal alertWarningPrice;

    private BigDecimal alertCriticalPrice;
}
