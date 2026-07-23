package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 做T交易记录保存 DTO
 */
@Data
public class StockTradeRecordSaveDTO {

    /** 关联规则表 ID */
    @NotNull(message = "规则ID不能为空")
    private Long configId;

    /** 关联档位表 ID（满档强制卖出时不传） */
    private Long operationId;

    /** 买卖方向 1-买入 2-卖出 */
    @NotNull(message = "交易方向不能为空")
    private Integer direction;

    /** 操作股数 */
    @NotNull(message = "股数不能为空")
    private Integer shares;

    /** 成交价 */
    @NotNull(message = "成交价不能为空")
    private BigDecimal price;

    /** 买卖理由 */
    @NotBlank(message = "买卖理由不能为空")
    private String reason;

    /** 交易时间 */
    @NotBlank(message = "交易时间不能为空")
    private String tradeTime;
}
