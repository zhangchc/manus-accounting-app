package com.accounting.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoVO {

    private Long userId;

    private String username;

    private String nickname;

    private List<MenuVO> menus;

    private List<String> permissions;
}
