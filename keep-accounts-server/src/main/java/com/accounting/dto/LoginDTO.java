package com.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {

    @NotBlank(message = "code不能为空")
    private String code;

    private String nickName;

    private String avatarUrl;

    private Integer gender;
}
