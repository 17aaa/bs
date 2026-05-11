package com.zhm.springboot.userservice.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.dto.AdminUserUpdateDTO;
import com.zhm.springboot.userservice.admin.dto.BanUserDTO;
import com.zhm.springboot.userservice.admin.vo.AdminUserDetailVO;
import com.zhm.springboot.userservice.admin.vo.AdminUserVO;

/**
 * 管理端用户服务接口
 */
public interface AdminUserService {

    /**
     * 获取用户列表
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @param role 角色筛选
     */
    Page<AdminUserVO> getUserList(Page<AdminUserVO> page, String keyword, Integer status, String role);

    /**
     * 获取用户详情
     * @param userId 用户 ID
     */
    AdminUserDetailVO getUserDetail(Long userId);

    /**
     * 更新用户信息
     * @param userId 用户 ID
     * @param dto 更新数据
     */
    boolean updateUser(Long userId, AdminUserUpdateDTO dto);

    /**
     * 重置用户密码
     * @param userId 用户 ID
     * @param newPassword 新密码
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 封禁用户
     * @param userId 用户 ID
     * @param dto 封禁信息
     */
    boolean banUser(Long userId, BanUserDTO dto);

    /**
     * 解封用户
     * @param userId 用户 ID
     */
    boolean unbanUser(Long userId);

    /**
     * 升级为管理员
     * @param userId 用户 ID
     */
    boolean upgradeToAdmin(Long userId);

    /**
     * 降级为普通用户
     * @param userId 用户 ID
     */
    boolean downgradeToUser(Long userId);
}