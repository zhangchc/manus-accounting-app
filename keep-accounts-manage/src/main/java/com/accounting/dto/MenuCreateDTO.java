package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class MenuCreateDTO {

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    private String icon;

    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "dir|menu|btn", message = "菜单类型必须为 dir、menu 或 btn")
    private String type;

    private String path;

    private String component;

    private String permission;

    private Integer sort;

    private Boolean status;
}
