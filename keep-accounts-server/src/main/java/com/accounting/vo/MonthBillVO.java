package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 月账单汇总VO
 */
@Data
public class MonthBillVO {

    private String yearMonth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal budget;
    private BigDecimal budgetRemain;
    private List<DailyBillVO> dailyList;
}
