package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecordVO {

    private Long id;

    private String userNickName;

    private String userAvatarUrl;

    private Long categoryId;

    private String categoryName;

    private String categoryIcon;

    private Integer type;

    private BigDecimal amount;

    private String remark;

    private Long bookId;

    private String bookName;

    private String recordTime;
}
