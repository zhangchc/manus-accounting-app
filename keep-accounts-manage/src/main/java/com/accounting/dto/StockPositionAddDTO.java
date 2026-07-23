package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 股票持仓新增 DTO
 */
@Data
public class StockPositionAddDTO {

    /** 股票代码，如 600519 */
    @NotBlank(message = "股票代码不能为空")
    private String stockCode;

    /** 股票名称 */
    @NotBlank(message = "股票名称不能为空")
    private String stockName;

    /** 持仓股数 */
    @NotNull(message = "持仓股数不能为空")
    private Integer shares;

    /** 成本价（元） */
    @NotNull(message = "成本价不能为空")
    private BigDecimal costPrice;
}
