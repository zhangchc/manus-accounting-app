package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 股票持仓修改 / 删除 DTO
 * <p>deleted=0 时更新字段，deleted=1 时逻辑删除</p>
 */
@Data
public class StockPositionSaveDTO {

    /** 目标持仓 ID */
    @NotNull(message = "ID不能为空")
    private Long id;

    /** 股票名称（修改时传） */
    private String stockName;

    /** 持仓股数（修改时传） */
    private Integer shares;

    /** 成本价（修改时传） */
    private BigDecimal costPrice;

    /** 0=修改字段，1=逻辑删除 */
    @NotNull(message = "操作标识不能为空")
    private Integer deleted;
}
