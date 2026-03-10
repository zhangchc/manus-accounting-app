package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统计数据VO
 */
@Data
public class StatisticsVO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private List<CategoryStatVO> categoryList;
    private List<DayTrendVO> trendList;

    /**
     * 分类统计
     */
    @Data
    public static class CategoryStatVO {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private BigDecimal amount;
        private String percent;
    }

    /**
     * 日趋势
     */
    @Data
    public static class DayTrendVO {
        private String date;
        private BigDecimal amount;
    }
}
