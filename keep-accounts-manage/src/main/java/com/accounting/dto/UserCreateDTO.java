package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String email;

    private String phone;

    private Boolean status;
}
