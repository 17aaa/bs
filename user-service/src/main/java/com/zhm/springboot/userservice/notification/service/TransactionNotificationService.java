package com.zhm.springboot.userservice.notification.service;

import com.zhm.springboot.userservice.notification.entity.TransactionNotification;
import com.zhm.springboot.userservice.notification.vo.NotificationVO;

import java.math.BigInteger;
import java.util.List;

/**
 * 交易通知服务接口
 */
public interface TransactionNotificationService {

    /**
     * 创建购买通知
     */
    TransactionNotification createPurchaseNotification(Long buyerId, String orderId, Long nftAssetId,
                                                       String nftName, String nftImageUrl, BigInteger amount,
                                                       String sellerAddress, String txHash);

    /**
     * 创建出售通知
     */
    TransactionNotification createSaleNotification(Long sellerId, String orderId, Long nftAssetId,
                                                   String nftName, String nftImageUrl, BigInteger amount,
                                                   String buyerAddress, String txHash);

    /**
     * 创建版税通知
     */
    TransactionNotification createRoyaltyNotification(Long creatorId, Long nftAssetId, String nftName,
                                                      String nftImageUrl, BigInteger royaltyAmount, String txHash);

    /**
     * 获取用户通知列表
     */
    List<NotificationVO> getUserNotifications(Long userId, int page, int size);

    /**
     * 获取未读通知数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId, Long userId);
}