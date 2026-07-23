package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 做T规则保存 DTO（新增 / 修改统一入口）
 */
@Data
public class StockTradeConfigSaveDTO {

    /** 股票代码 */
    @NotBlank(message = "股票代码不能为空")
    private String stockCode;

    /** 股票名称 */
    @NotBlank(message = "股票名称不能为空")
    private String stockName;

    /** 基准价（网格中枢） */
    @NotNull(message = "基准价不能为空")
    private BigDecimal basePrice;

    /** 档位数 */
    @NotNull(message = "档位数不能为空")
    private Integer levels;

    /** 卖出每档涨幅 % */
    @NotNull(message = "卖出每档涨幅不能为空")
    private BigDecimal upPct;

    /** 买入每档跌幅 % */
    @NotNull(message = "买入每档跌幅不能为空")
    private BigDecimal downPct;

    /** 每档操作固定股数 */
    @NotNull(message = "操作股数不能为空")
    private Integer fixedShares;

    /** 状态 0-停用 1-启用 */
    private Integer active;
}
