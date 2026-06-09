package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AppUserDetailVO {

    private Long totalRecords;

    private Long bookCount;

    private BigDecimal totalExpense;

    private BigDecimal totalIncome;

    private List<RecordItemVO> recentRecords;

    @Data
    public static class RecordItemVO {

        private Long id;

        private String categoryName;

        private String categoryIcon;

        private Integer type;

        private BigDecimal amount;

        private String remark;

        private String recordTime;
    }
}
