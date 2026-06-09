package com.accounting.common;

import com.accounting.service.MenuService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private MenuService menuService;

    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(com.accounting.common.RequirePermission)")
    public Object check(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        RequirePermission annotation = signature.getMethod().getAnnotation(RequirePermission.class);

        Long userId = (Long) request.getAttribute("userId");
        Set<String> codes = menuService.getPermissionCodesByUserId(userId);

        if (!codes.contains(annotation.value())) {
            throw new BusinessException(403, "无权限");
        }

        return pjp.proceed();
    }
}
