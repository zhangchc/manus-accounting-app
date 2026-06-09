package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class AppUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openId;

    private String unionId;

    private String nickName;

    private String avatarUrl;

    private Integer gender;

    private BigDecimal monthlyBudget;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
