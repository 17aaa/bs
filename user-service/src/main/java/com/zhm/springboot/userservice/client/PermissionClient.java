package com.zhm.springboot.userservice.client;

import com.zhm.springboot.userservice.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 权限客户端（本地实现）
 * 原本通过 Feign 调用 permission-service，现改为本地实现
 */
@Component
@Slf4j
public class PermissionClient {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 绑定默认角色
     */
    public String bindDefaultRole(Long userId) {
        return userRoleService.bindDefaultRole(userId);
    }

    /**
     * 获取用户角色
     */
    public String getUserRole(Long userId) {
        log.info("PermissionClient.getUserRole: userId={}", userId);
        String role = userRoleService.getUserRole(userId);
        log.info("PermissionClient.getUserRole result: userId={}, role={}", userId, role);
        return role;
    }

    /**
     * 升级为管理员
     */
    public void upgradeToAdmin(Long userId) {
        userRoleService.upgradeToAdmin(userId);
    }

    /**
     * 降级为普通用户
     */
    public void downgradeToUser(Long userId) {
        userRoleService.downgradeToUser(userId);
    }

    /**
     * 升级为超级管理员
     */
    public void upgradeToSuperAdmin(Long userId) {
        userRoleService.upgradeToSuperAdmin(userId);
    }
}