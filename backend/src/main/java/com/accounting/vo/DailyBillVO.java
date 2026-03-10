package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 日账单汇总VO
 */
@Data
public class DailyBillVO {

    private String date;
    private String weekDay;
    private BigDecimal dayIncome;
    private BigDecimal dayExpense;
    private List<RecordVO> records;
}
