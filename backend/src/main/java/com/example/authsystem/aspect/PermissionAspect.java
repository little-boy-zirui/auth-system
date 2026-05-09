package com.example.authsystem.aspect;

import com.example.authsystem.annotation.RequirePermission;
import com.example.authsystem.model.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class PermissionAspect {

    private final HttpServletRequest request;

    public PermissionAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Around("@annotation(com.example.authsystem.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequirePermission requirePermission = signature.getMethodAnnotation(RequirePermission.class);

        String[] requiredPermissions = requirePermission.value();
        String logical = requirePermission.logical();

        SessionUser sessionUser = getSessionUser();
        if (sessionUser == null) {
            throw new SecurityException("用户未登录");
        }

        List<String> userPermissions = sessionUser.permissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            throw new SecurityException("用户没有任何权限");
        }

        boolean hasPermission;
        if ("AND".equalsIgnoreCase(logical)) {
            hasPermission = Arrays.stream(requiredPermissions)
                .allMatch(userPermissions::contains);
        } else {
            hasPermission = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            throw new SecurityException("权限不足：需要 " + String.join(", ", requiredPermissions));
        }

        return joinPoint.proceed();
    }

    private SessionUser getSessionUser() {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        Object sessionUser = request.getSession(false).getAttribute("SESSION_AUTH_USER");
        if (sessionUser instanceof SessionUser) {
            return (SessionUser) sessionUser;
        }
        return null;
    }
}
