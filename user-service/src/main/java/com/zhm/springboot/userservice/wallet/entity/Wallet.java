package com.zhm.springboot.userservice.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户钱包实体类
 */
@Data
@TableName("wallets")
public class Wallet {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * Keystore 路径
     */
    private String keystorePath;

    /**
     * 是否已绑定
     */
    private Boolean isBound;

    /**
     * 绑定时间
     */
    private LocalDateTime boundAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}