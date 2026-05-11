package com.zhm.springboot.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhm.springboot.userservice.entity.UserRole;
import com.zhm.springboot.userservice.mapper.UserRoleMapper;
import com.zhm.springboot.userservice.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户角色服务实现
 */
@Service
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public String getUserRole(Long userId) {
        if (userId == null) {
            return "user";
        }

        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        UserRole userRole = userRoleMapper.selectOne(wrapper);

        if (userRole == null) {
            log.warn("用户角色不存在，返回默认角色: userId={}", userId);
            return "user";
        }

        return userRole.getRole();
    }

    @Override
    public String bindDefaultRole(Long userId) {
        log.info("绑定默认角色: userId={}", userId);

        // 检查是否已存在
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        UserRole existing = userRoleMapper.selectOne(wrapper);

        if (existing != null) {
            log.info("用户角色已存在: userId={}, role={}", userId, existing.getRole());
            return existing.getRole();
        }

        // 创建默认角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRole("user");
        userRoleMapper.insert(userRole);

        log.info("默认角色绑定成功: userId={}", userId);
        return "user";
    }

    @Override
    public void upgradeToAdmin(Long userId) {
        log.info("升级为管理员: userId={}", userId);
        updateRole(userId, "admin");
    }

    @Override
    public void downgradeToUser(Long userId) {
        log.info("降级为普通用户: userId={}", userId);
        updateRole(userId, "user");
    }

    @Override
    public void upgradeToSuperAdmin(Long userId) {
        log.info("升级为超级管理员: userId={}", userId);
        updateRole(userId, "super_admin");
    }

    private void updateRole(Long userId, String role) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        UserRole userRole = userRoleMapper.selectOne(wrapper);

        if (userRole == null) {
            // 不存在则创建
            userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRole(role);
            userRoleMapper.insert(userRole);
        } else {
            // 更新角色
            userRole.setRole(role);
            userRoleMapper.updateById(userRole);
        }

        log.info("角色更新成功: userId={}, role={}", userId, role);
    }
}