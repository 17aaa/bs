package com.zhm.springboot.userservice.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhm.springboot.userservice.audit.entity.AuditLog;
import com.zhm.springboot.userservice.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 操作日志消费者
 * 消费 UserController 发送的用户操作事件消息，写入审计日志表
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rocketmq.consumer.enabled", havingValue = "true", matchIfMissing = false)
@RocketMQMessageListener(topic = "user-operation-topic", consumerGroup = "user-service-consumer")
@RequiredArgsConstructor
public class OperationLogConsumer implements RocketMQListener {

    private final AuditLogService auditLogService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConsumeResult consume(MessageView messageView) {
        try {
            byte[] body = messageView.getBody().array();
            String json = new String(body, StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> logMap = objectMapper.readValue(json, Map.class);

            Long userId = logMap.get("user_id") != null ? Long.valueOf(logMap.get("user_id").toString()) : null;
            String action = (String) logMap.getOrDefault("action", "unknown");
            String detail = (String) logMap.getOrDefault("detail", "");

            String module = resolveModule(action);
            auditLogService.log(userId, action, module, detail);
            log.info("消费操作日志: userId={}, action={}, module={}", userId, action, module);
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            log.error("消费操作日志失败: {}", e.getMessage(), e);
            return ConsumeResult.FAILURE;
        }
    }

    private String resolveModule(String action) {
        if (action == null) return AuditLog.MODULE_USER;
        switch (action) {
            case "login":
            case "logout":
                return AuditLog.MODULE_AUTH;
            case "register":
            case "update_user":
            case "reset_password":
            case "role_upgrade":
            case "role_downgrade":
                return AuditLog.MODULE_USER;
            default:
                return AuditLog.MODULE_USER;
        }
    }
}