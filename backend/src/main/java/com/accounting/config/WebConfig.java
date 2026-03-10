package com.accounting.config;

import com.accounting.common.BusinessException;
import com.accounting.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Web配置 - 登录拦截器
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/wxLogin",
                        "/error"
                );
    }

    /**
     * 认证拦截器
     */
    private class AuthInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // 放行 OPTIONS 请求
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }

            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                throw new BusinessException(401, "未登录或登录已过期");
            }

            // 去掉 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(401, "未登录或登录已过期");
            }

            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                throw new BusinessException(401, "未登录或登录已过期");
            }

            // 将userId放入request属性中
            request.setAttribute("userId", userId);
            return true;
        }
    }
}
