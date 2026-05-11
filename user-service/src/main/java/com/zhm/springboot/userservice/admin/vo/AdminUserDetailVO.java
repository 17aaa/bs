package com.zhm.springboot.userservice.admin.vo;

import lombok.Data;
import java.sql.Timestamp;
import java.math.BigInteger;

/**
 * 管理端用户详情 VO
 */
@Data
public class AdminUserDetailVO {
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
     * 简介
     */
    private String bio;

    /**
     * 用户状态：1-正常 2-禁用
     */
    private Integer status;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 封禁时间
     */
    private Timestamp bannedAt;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 创建时间
     */
    private Timestamp gmtCreate;

    // 钱包信息
    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 钱包创建时间
     */
    private Timestamp walletCreatedAt;

    // NFT 统计
    /**
     * 创建的 NFT 数量
     */
    private Integer createdNftCount;

    /**
     * 持有的 NFT 数量
     */
    private Integer ownedNftCount;

    // 交易统计
    /**
     * 作为卖家的交易次数
     */
    private Integer sellerTradeCount;

    /**
     * 作为买家的交易次数
     */
    private Integer buyerTradeCount;

    /**
     * 总交易额（Wei）
     */
    private BigInteger totalTradeVolume;
}