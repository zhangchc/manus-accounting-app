package com.accounting.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.MDC;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一记录 controller 方法的入参/出参（AOP）。
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ControllerIoLoggingAspect {

    private final ObjectMapper objectMapper;
    private final ControllerIoLoggingProperties properties;

    public ControllerIoLoggingAspect(ObjectMapper objectMapper, ControllerIoLoggingProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Around("execution(* com.accounting.controller..*(..))")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        if (!properties.isEnabled()) {
            return pjp.proceed();
        }

        HttpServletRequest request = currentRequest();
        String requestUri = request != null ? request.getRequestURI() : null;
        if (isExcluded(requestUri)) {
            return pjp.proceed();
        }

        long startNs = System.nanoTime();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String methodName = signature.getDeclaringTypeName() + "#" + method.getName();

        Long userId = request != null ? (Long) request.getAttribute("userId") : null;
        String httpMethod = request != null ? request.getMethod() : null;
        String query = request != null ? request.getQueryString() : null;
        String clientIp = request != null ? resolveClientIp(request) : null;
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);

        Map<String, Object> argsMap = null;
        if (properties.isLogArgs()) {
            argsMap = buildArgsMap(signature, pjp.getArgs());
            log.info("REQ traceId={} method={} uri={} query={} clientIp={} userId={} handler={} args={}",
                    traceId, httpMethod, requestUri, query, clientIp, userId, methodName, truncate(toJson(argsMap)));
        } else {
            log.info("REQ traceId={} method={} uri={} query={} clientIp={} userId={} handler={}",
                    traceId, httpMethod, requestUri, query, clientIp, userId, methodName);
        }

        try {
            Object result = pjp.proceed();
            long costMs = (System.nanoTime() - startNs) / 1_000_000;
            if (properties.isLogResult()) {
                log.info("RESP traceId={} method={} uri={} userId={} handler={} costMs={} result={}",
                        traceId, httpMethod, requestUri, userId, methodName, costMs, truncate(toJson(result)));
            } else {
                log.info("RESP traceId={} method={} uri={} userId={} handler={} costMs={}",
                        traceId, httpMethod, requestUri, userId, methodName, costMs);
            }
            return result;
        } catch (Throwable t) {
            long costMs = (System.nanoTime() - startNs) / 1_000_000;
            log.error("EX traceId={} method={} uri={} userId={} handler={} costMs={} msg={}",
                    traceId, httpMethod, requestUri, userId, methodName, costMs, t.getMessage(), t);
            throw t;
        }
    }

    private boolean isExcluded(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        if (properties.getExcludePaths() == null) {
            return false;
        }
        for (String p : properties.getExcludePaths()) {
            if (p != null && !p.isEmpty() && requestUri.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attrs).getRequest();
        }
        return null;
    }

    private Map<String, Object> buildArgsMap(MethodSignature signature, Object[] args) {
        String[] names = signature.getParameterNames();
        Map<String, Object> map = new LinkedHashMap<>();
        if (args == null || args.length == 0) {
            return map;
        }
        for (int i = 0; i < args.length; i++) {
            Object v = args[i];
            if (shouldSkip(v)) {
                continue;
            }
            String k = (names != null && names.length > i && names[i] != null) ? names[i] : ("arg" + i);
            map.put(k, normalizeArg(v));
        }
        return map;
    }

    private boolean shouldSkip(Object v) {
        return v == null
                || v instanceof ServletRequest
                || v instanceof ServletResponse
                || v instanceof HttpServletRequest
                || v instanceof BindingResult;
    }

    private Object normalizeArg(Object v) {
        if (v instanceof MultipartFile) {
            MultipartFile f = (MultipartFile) v;
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("name", f.getName());
            meta.put("originalFilename", f.getOriginalFilename());
            meta.put("size", f.getSize());
            meta.put("contentType", f.getContentType());
            return meta;
        }
        if (v instanceof MultipartFile[]) {
            MultipartFile[] fs = (MultipartFile[]) v;
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("count", fs.length);
            return meta;
        }
        return v;
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        int max = properties.getMaxLen();
        if (max <= 0 || s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...(truncated,len=" + s.length() + ")";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            int idx = xff.indexOf(',');
            return idx > 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

