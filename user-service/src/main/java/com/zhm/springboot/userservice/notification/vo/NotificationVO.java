package com.zhm.springboot.userservice.notification.vo;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 通知 VO
 */
@Data
public class NotificationVO {

    private Long id;

    private String type;

    private String typeLabel;

    private String orderId;

    private Long nftAssetId;

    private String nftName;

    private String nftImageUrl;

    private BigInteger amount;

    private String amountFormatted;

    private String counterpartyAddress;

    private String txHash;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private String timeAgo;
}