package com.accounting.config;

import com.accounting.common.BusinessException;
import com.accounting.service.UserService;
import com.accounting.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Web配置 - 登录拦截器
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Value("${upload.avatar-dir:upload/avatar}")
    private String avatarDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File dir = new File(avatarDir);
        String absolutePath = dir.getAbsolutePath();
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absolutePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/wxLogin",
                        "/upload/**",
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
            if (userService.getUserInfo(userId) == null) {
                throw new BusinessException(401, "未登录或登录已过期");
            }

            // 将userId放入request属性中
            request.setAttribute("userId", userId);
            return true;
        }
    }
}
