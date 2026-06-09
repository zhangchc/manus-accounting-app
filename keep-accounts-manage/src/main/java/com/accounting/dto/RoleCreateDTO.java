package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z][A-Z_]*$", message = "角色编码必须为大写字母和下划线")
    private String code;

    private String desc;

    private Integer sort;

    private Boolean status;
}
