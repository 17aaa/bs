package com.zhm.springboot.userservice.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhm.springboot.userservice.notification.entity.TransactionNotification;
import com.zhm.springboot.userservice.notification.mapper.TransactionNotificationMapper;
import com.zhm.springboot.userservice.notification.service.TransactionNotificationService;
import com.zhm.springboot.userservice.notification.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionNotificationServiceImpl implements TransactionNotificationService {

    private final TransactionNotificationMapper notificationMapper;

    private static final Map<String, String> TYPE_LABELS = new HashMap<>();
    static {
        TYPE_LABELS.put(TransactionNotification.TYPE_PURCHASE, "购买成功");
        TYPE_LABELS.put(TransactionNotification.TYPE_SALE, "出售成功");
        TYPE_LABELS.put(TransactionNotification.TYPE_BID, "新出价");
        TYPE_LABELS.put(TransactionNotification.TYPE_TRANSFER, "NFT转移");
        TYPE_LABELS.put(TransactionNotification.TYPE_ROYALTY, "版税收入");
    }

    @Override
    public TransactionNotification createPurchaseNotification(Long buyerId, String orderId, Long nftAssetId,
                                                              String nftName, String nftImageUrl, BigInteger amount,
                                                              String sellerAddress, String txHash) {
        TransactionNotification notification = new TransactionNotification();
        notification.setUserId(buyerId);
        notification.setType(TransactionNotification.TYPE_PURCHASE);
        notification.setOrderId(orderId);
        notification.setNftAssetId(nftAssetId);
        notification.setNftName(nftName);
        notification.setNftImageUrl(nftImageUrl);
        notification.setAmount(amount);
        notification.setCounterpartyAddress(sellerAddress);
        notification.setTxHash(txHash);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("创建购买通知: userId={}, nftName={}", buyerId, nftName);
        return notification;
    }

    @Override
    public TransactionNotification createSaleNotification(Long sellerId, String orderId, Long nftAssetId,
                                                          String nftName, String nftImageUrl, BigInteger amount,
                                                          String buyerAddress, String txHash) {
        TransactionNotification notification = new TransactionNotification();
        notification.setUserId(sellerId);
        notification.setType(TransactionNotification.TYPE_SALE);
        notification.setOrderId(orderId);
        notification.setNftAssetId(nftAssetId);
        notification.setNftName(nftName);
        notification.setNftImageUrl(nftImageUrl);
        notification.setAmount(amount);
        notification.setCounterpartyAddress(buyerAddress);
        notification.setTxHash(txHash);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("创建出售通知: userId={}, nftName={}", sellerId, nftName);
        return notification;
    }

    @Override
    public TransactionNotification createRoyaltyNotification(Long creatorId, Long nftAssetId, String nftName,
                                                             String nftImageUrl, BigInteger royaltyAmount, String txHash) {
        TransactionNotification notification = new TransactionNotification();
        notification.setUserId(creatorId);
        notification.setType(TransactionNotification.TYPE_ROYALTY);
        notification.setNftAssetId(nftAssetId);
        notification.setNftName(nftName);
        notification.setNftImageUrl(nftImageUrl);
        notification.setAmount(royaltyAmount);
        notification.setTxHash(txHash);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("创建版税通知: userId={}, nftName={}, amount={}", creatorId, nftName, royaltyAmount);
        return notification;
    }

    @Override
    public List<NotificationVO> getUserNotifications(Long userId, int page, int size) {
        int offset = (page - 1) * size;

        QueryWrapper<TransactionNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("created_at")
               .last("LIMIT " + size + " OFFSET " + offset);

        List<TransactionNotification> notifications = notificationMapper.selectList(wrapper);
        List<NotificationVO> vos = new ArrayList<>();

        for (TransactionNotification notification : notifications) {
            vos.add(convertToVO(notification));
        }

        return vos;
    }

    @Override
    public long getUnreadCount(Long userId) {
        QueryWrapper<TransactionNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("is_read", false);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    public void markAsRead(Long notificationId) {
        TransactionNotification notification = notificationMapper.selectById(notificationId);
        if (notification != null) {
            notification.setIsRead(true);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        QueryWrapper<TransactionNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("is_read", false);

        List<TransactionNotification> notifications = notificationMapper.selectList(wrapper);
        for (TransactionNotification notification : notifications) {
            notification.setIsRead(true);
            notificationMapper.updateById(notification);
        }

        log.info("标记所有通知为已读: userId={}, count={}", userId, notifications.size());
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        QueryWrapper<TransactionNotification> wrapper = new QueryWrapper<>();
        wrapper.eq("id", notificationId)
               .eq("user_id", userId);
        notificationMapper.delete(wrapper);
    }

    private NotificationVO convertToVO(TransactionNotification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTypeLabel(TYPE_LABELS.getOrDefault(notification.getType(), "交易通知"));
        vo.setOrderId(notification.getOrderId());
        vo.setNftAssetId(notification.getNftAssetId());
        vo.setNftName(notification.getNftName());
        vo.setNftImageUrl(notification.getNftImageUrl());
        vo.setAmount(notification.getAmount());
        vo.setAmountFormatted(formatAmount(notification.getAmount()));
        vo.setCounterpartyAddress(notification.getCounterpartyAddress());
        vo.setTxHash(notification.getTxHash());
        vo.setIsRead(notification.getIsRead());
        vo.setCreatedAt(notification.getCreatedAt());
        vo.setTimeAgo(formatTimeAgo(notification.getCreatedAt()));
        return vo;
    }

    private String formatAmount(BigInteger amount) {
        if (amount == null) return "0";
        BigDecimal eth = new BigDecimal(amount)
                .divide(BigDecimal.valueOf(1e18), 6, RoundingMode.DOWN);
        return eth.stripTrailingZeros().toPlainString() + " ETH";
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟前";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小时前";
        } else if (seconds < 604800) {
            return (seconds / 86400) + "天前";
        } else {
            return (seconds / 604800) + "周前";
        }
    }
}