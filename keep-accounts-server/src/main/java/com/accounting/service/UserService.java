package com.accounting.service;

import com.accounting.dto.LoginDTO;
import com.accounting.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 微信登录
     */
    Map<String, Object> wxLogin(LoginDTO loginDTO);

    /**
     * 模拟登录（开发调试用）
     */
    Map<String, Object> devLogin(LoginDTO loginDTO);

    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, User user);
}
