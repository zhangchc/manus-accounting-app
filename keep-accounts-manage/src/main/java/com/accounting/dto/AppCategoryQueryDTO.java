package com.accounting.dto;

import lombok.Data;

@Data
public class AppCategoryQueryDTO {

    private String name;

    private Integer type;

    private Integer page = 1;

    private Integer pageSize = 20;
}
