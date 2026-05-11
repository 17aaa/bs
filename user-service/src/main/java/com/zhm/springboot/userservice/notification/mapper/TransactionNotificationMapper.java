package com.zhm.springboot.userservice.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.notification.entity.TransactionNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易通知 Mapper
 */
@Mapper
public interface TransactionNotificationMapper extends BaseMapper<TransactionNotification> {
}