package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 做T操作档位 VO
 */
@Data
public class StockTradeOperationVO {

    /** 档位ID */
    private Long id;

    /** 关联规则ID */
    private Long configId;

    /** 股票代码 */
    private String stockCode;

    /** 档位编号 */
    private Integer levelNo;

    /** 买卖方向 1-买入 2-卖出 */
    private Integer direction;

    /** 档位价格 */
    private BigDecimal levelPrice;

    /** 是否已触发 0-未触发 1-已触发 */
    private Integer triggered;
}
