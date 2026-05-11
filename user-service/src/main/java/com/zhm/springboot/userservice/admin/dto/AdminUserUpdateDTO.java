package com.zhm.springboot.userservice.admin.dto;

import lombok.Data;

/**
 * 管理端更新用户信息 DTO
 */
@Data
public class AdminUserUpdateDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 学校
     */
    private String school;

    /**
     * 专业
     */
    private String major;

    /**
     * 简介
     */
    private String bio;
}