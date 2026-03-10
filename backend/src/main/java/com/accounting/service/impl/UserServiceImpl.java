package com.accounting.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.accounting.common.BusinessException;
import com.accounting.dto.LoginDTO;
import com.accounting.entity.Book;
import com.accounting.entity.User;
import com.accounting.mapper.UserMapper;
import com.accounting.service.BookService;
import com.accounting.service.UserService;
import com.accounting.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private BookService bookService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> wxLogin(LoginDTO loginDTO) {
        // 调用微信接口获取openId
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, loginDTO.getCode()
        );
        String result = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(result);

        String openId = jsonObject.getStr("openid");
        if (openId == null || openId.isEmpty()) {
            log.error("微信登录失败: {}", result);
            throw new BusinessException("微信登录失败");
        }

        return doLogin(openId, loginDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> devLogin(LoginDTO loginDTO) {
        // 开发模式：使用code作为openId
        String openId = "dev_" + loginDTO.getCode();
        return doLogin(openId, loginDTO);
    }

    private Map<String, Object> doLogin(String openId, LoginDTO loginDTO) {
        // 查询用户是否存在
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenId, openId));

        boolean isNewUser = false;
        if (user == null) {
            // 新用户注册
            isNewUser = true;
            user = new User();
            user.setOpenId(openId);
            user.setNickName(loginDTO.getNickName() != null ? loginDTO.getNickName() : "记账用户");
            user.setAvatarUrl(loginDTO.getAvatarUrl() != null ? loginDTO.getAvatarUrl() : "");
            user.setGender(loginDTO.getGender() != null ? loginDTO.getGender() : 0);
            user.setMonthlyBudget(BigDecimal.ZERO);
            this.save(user);

            // 为新用户创建默认账本
            Book defaultBook = new Book();
            defaultBook.setUserId(user.getId());
            defaultBook.setName("日常账本");
            defaultBook.setIcon("📒");
            defaultBook.setIsDefault(1);
            defaultBook.setSortOrder(0);
            bookService.save(defaultBook);
        } else {
            // 更新用户信息
            if (loginDTO.getNickName() != null) {
                user.setNickName(loginDTO.getNickName());
            }
            if (loginDTO.getAvatarUrl() != null) {
                user.setAvatarUrl(loginDTO.getAvatarUrl());
            }
            this.updateById(user);
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), openId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", token);
        resultMap.put("userInfo", user);
        resultMap.put("isNewUser", isNewUser);
        return resultMap;
    }

    @Override
    public User getUserInfo(Long userId) {
        return this.getById(userId);
    }

    @Override
    public void updateUserInfo(Long userId, User user) {
        user.setId(userId);
        // 不允许修改openId
        user.setOpenId(null);
        this.updateById(user);
    }
}
