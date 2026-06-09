package com.accounting.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private Boolean status;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

    private List<String> roleNames;
}
