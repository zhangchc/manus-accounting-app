package com.accounting.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppCategoryVO {

    private Long id;

    private Long userId;

    private String name;

    private String icon;

    private Integer type;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
