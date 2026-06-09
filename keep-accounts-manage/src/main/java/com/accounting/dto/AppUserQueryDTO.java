package com.accounting.dto;

import lombok.Data;

@Data
public class AppUserQueryDTO {

    private String nickName;

    private String openId;

    private String startDate;

    private String endDate;

    private Integer page = 1;

    private Integer pageSize = 10;
}
