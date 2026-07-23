package com.accounting.dto;

import lombok.Data;

/**
 * 股票持仓查询 DTO
 */
@Data
public class StockPositionQueryDTO {

    /** 股票代码，模糊匹配 */
    private String stockCode;

    /** 股票名称，模糊匹配 */
    private String stockName;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 10 */
    private Integer pageSize = 10;
}
