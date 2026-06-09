package com.accounting.vo;

import lombok.Data;

@Data
public class RoleVO {

    private Long id;

    private String name;

    private String code;

    private String desc;

    private Integer sort;

    private Boolean status;
}
