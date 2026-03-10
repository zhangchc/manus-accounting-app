package com.accounting.controller;

import com.accounting.common.Result;
import com.accounting.dto.LoginDTO;
import com.accounting.entity.User;
import com.accounting.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 微信登录
     */
    @PostMapping("/wxLogin")
    public Result<Map<String, Object>> wxLogin(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.wxLogin(loginDTO);
        return Result.success(result);
    }

    /**
     * 开发模式登录（调试用）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> devLogin(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.devLogin(loginDTO);
        return Result.success(result);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserInfo(userId, user);
        return Result.success();
    }
}
