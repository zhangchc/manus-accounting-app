package com.accounting.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppUserVO {

    private Long id;

    private String openId;

    private String unionId;

    private String nickName;

    private String avatarUrl;

    private Integer gender;

    private BigDecimal monthlyBudget;

    private LocalDateTime createTime;
}
