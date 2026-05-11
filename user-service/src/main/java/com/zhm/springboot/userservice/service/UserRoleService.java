package com.zhm.springboot.userservice.service;

import com.zhm.springboot.userservice.entity.UserRole;

/**
 * 用户角色服务接口
 */
public interface UserRoleService {

    /**
     * 获取用户角色
     * @param userId 用户ID
     * @return 角色名称
     */
    String getUserRole(Long userId);

    /**
     * 绑定默认角色
     * @param userId 用户ID
     * @return 角色名称
     */
    String bindDefaultRole(Long userId);

    /**
     * 升级为管理员
     * @param userId 用户ID
     */
    void upgradeToAdmin(Long userId);

    /**
     * 降级为普通用户
     * @param userId 用户ID
     */
    void downgradeToUser(Long userId);

    /**
     * 升级为超级管理员
     * @param userId 用户ID
     */
    void upgradeToSuperAdmin(Long userId);
}