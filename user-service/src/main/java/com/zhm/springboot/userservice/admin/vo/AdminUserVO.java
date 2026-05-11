package com.zhm.springboot.userservice.admin.vo;

import lombok.Data;
import java.sql.Timestamp;

/**
 * 管理端用户列表 VO
 */
@Data
public class AdminUserVO {
    /**
     * 用户 ID
     */
    private Long userId;

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
     * 头像
     */
    private String avatar;

    /**
     * 学校
     */
    private String school;

    /**
     * 专业
     */
    private String major;

    /**
     * 用户状态：1-正常 2-禁用
     */
    private Integer status;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 创建时间
     */
    private Timestamp gmtCreate;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * NFT 数量
     */
    private Integer nftCount;

    /**
     * 交易次数
     */
    private Integer tradeCount;
}