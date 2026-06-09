package com.accounting.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单响应 VO，与前端 mockMenus 字段完全对齐
 */
@Data
public class MenuVO {

    private Long id;

    private Long parentId;

    private String name;

    private String icon;

    private String type;

    private String path;

    private String component;

    private String permission;

    private Integer sort;

    private Boolean status;

    private List<MenuVO> children;
}
