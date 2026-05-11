package com.zhm.springboot.userservice.admin.interceptor;

import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.admin.annotation.SuperAdminOnly;
import com.zhm.springboot.userservice.client.PermissionClient;
import com.zhm.springboot.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端权限拦截器
 */
@Component
@Slf4j
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private PermissionClient permissionClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理 Controller 方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法上的注解
        AdminOnly adminOnly = handlerMethod.getMethodAnnotation(AdminOnly.class);
        SuperAdminOnly superAdminOnly = handlerMethod.getMethodAnnotation(SuperAdminOnly.class);

        // 检查类上的注解
        if (adminOnly == null) {
            adminOnly = handlerMethod.getBeanType().getAnnotation(AdminOnly.class);
        }
        if (superAdminOnly == null) {
            superAdminOnly = handlerMethod.getBeanType().getAnnotation(SuperAdminOnly.class);
        }

        // 如果没有权限注解，放行
        if (adminOnly == null && superAdminOnly == null) {
            return true;
        }

        // 获取 Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少 Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"未登录\"}");
            return false;
        }

        String token = authHeader.substring(7);
        Long userId;
        try {
            userId = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Token 无效\"}");
            return false;
        }

        // 获取用户角色
        String role;
        try {
            role = permissionClient.getUserRole(userId);
            log.info("获取用户角色成功: userId={}, role={}", userId, role);
        } catch (Exception e) {
            log.error("获取用户角色失败: userId={}, error={}", userId, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"error\",\"message\":\"权限服务不可用\"}");
            return false;
        }

        // 检查超级管理员权限
        if (superAdminOnly != null) {
            if (!"super_admin".equals(role)) {
                log.warn("权限不足: userId={}, role={}, required=super_admin", userId, role);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":\"error\",\"message\":\"需要超级管理员权限\"}");
                return false;
            }
        }

        // 检查管理员权限
        if (adminOnly != null) {
            if (!"admin".equals(role) && !"super_admin".equals(role)) {
                log.warn("权限不足: userId={}, role={}, required=admin", userId, role);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":\"error\",\"message\":\"需要管理员权限\"}");
                return false;
            }
        }

        // 将用户信息存入 request
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUserRole", role);

        return true;
    }
}