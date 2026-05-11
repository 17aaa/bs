package com.zhm.springboot.userservice.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.admin.annotation.SuperAdminOnly;
import com.zhm.springboot.userservice.admin.dto.AdminUserUpdateDTO;
import com.zhm.springboot.userservice.admin.dto.BanUserDTO;
import com.zhm.springboot.userservice.admin.service.AdminUserService;
import com.zhm.springboot.userservice.admin.vo.AdminUserDetailVO;
import com.zhm.springboot.userservice.admin.vo.AdminUserVO;
import com.zhm.springboot.userservice.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端用户控制器
 */
@RestController
@RequestMapping("/admin/users")
@AdminOnly
@Slf4j
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 获取用户列表
     */
    @GetMapping
    public ApiResponse<Page<AdminUserVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String role,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询用户列表: operator={}, page={}/{}, keyword={}, status={}, role={}",
                currentUserId, pageNum, pageSize, keyword, status, role);

        Page<AdminUserVO> page = new Page<>(pageNum, pageSize);
        Page<AdminUserVO> result = adminUserService.getUserList(page, keyword, status, role);
        return ApiResponse.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserDetailVO> getUserDetail(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询用户详情: operator={}, target={}", currentUserId, userId);

        AdminUserDetailVO detail = adminUserService.getUserDetail(userId);
        if (detail == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(detail);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ApiResponse<Void> updateUser(
            @PathVariable Long userId,
            @RequestBody AdminUserUpdateDTO dto,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("更新用户信息: operator={}, target={}", currentUserId, userId);

        boolean success = adminUserService.updateUser(userId, dto);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{userId}/reset-password")
    public ApiResponse<Void> resetPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("重置用户密码: operator={}, target={}", currentUserId, userId);

        try {
            boolean success = adminUserService.resetPassword(userId, newPassword);
            if (success) {
                return ApiResponse.success(null);
            }
            return ApiResponse.error("重置失败");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 封禁用户（超级管理员）
     */
    @PostMapping("/{userId}/ban")
    @SuperAdminOnly
    public ApiResponse<Void> banUser(
            @PathVariable Long userId,
            @RequestBody BanUserDTO dto,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("封禁用户: operator={}, target={}, reason={}", currentUserId, userId, dto.getReason());

        boolean success = adminUserService.banUser(userId, dto);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("封禁失败");
    }

    /**
     * 解封用户（超级管理员）
     */
    @PostMapping("/{userId}/unban")
    @SuperAdminOnly
    public ApiResponse<Void> unbanUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("解封用户: operator={}, target={}", currentUserId, userId);

        boolean success = adminUserService.unbanUser(userId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("解封失败");
    }

    /**
     * 升级为管理员（超级管理员）
     */
    @PostMapping("/{userId}/upgrade")
    @SuperAdminOnly
    public ApiResponse<Void> upgradeToAdmin(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("升级管理员: operator={}, target={}", currentUserId, userId);

        boolean success = adminUserService.upgradeToAdmin(userId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("升级失败");
    }

    /**
     * 降级为普通用户（超级管理员）
     */
    @PostMapping("/{userId}/downgrade")
    @SuperAdminOnly
    public ApiResponse<Void> downgradeToUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("降级用户: operator={}, target={}", currentUserId, userId);

        boolean success = adminUserService.downgradeToUser(userId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("降级失败");
    }
}