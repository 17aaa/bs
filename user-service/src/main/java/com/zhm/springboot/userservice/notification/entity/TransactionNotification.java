package com.zhm.springboot.userservice.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 交易通知实体
 */
@Data
@TableName("transaction_notifications")
public class TransactionNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知类型：purchase/sale/bid/transfer/royalty
     */
    private String type;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * NFT资产ID
     */
    private Long nftAssetId;

    /**
     * NFT名称
     */
    private String nftName;

    /**
     * NFT图片URL
     */
    private String nftImageUrl;

    /**
     * 交易金额（Wei）
     */
    private BigInteger amount;

    /**
     * 交易对方地址
     */
    private String counterpartyAddress;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // 通知类型常量
    public static final String TYPE_PURCHASE = "purchase";  // 购买通知
    public static final String TYPE_SALE = "sale";          // 出售通知
    public static final String TYPE_BID = "bid";            // 出价通知
    public static final String TYPE_TRANSFER = "transfer";  // 转移通知
    public static final String TYPE_ROYALTY = "royalty";    // 版税通知
}