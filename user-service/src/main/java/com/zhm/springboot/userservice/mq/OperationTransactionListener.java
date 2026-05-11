/** 向ai和网络资源学习，实现rocketmq5.系列的发送事务消息的监听器，但是由于未知原因，消费者接收不到，暂且搁置
package com.zhm.springboot.userservice.mq;

import com.zhm.springboot.userservice.entity.User;
import com.zhm.springboot.userservice.mapper.UserMapper;
import org.apache.rocketmq.client.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhm.springboot.userservice.service.UserService;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

@Component
@RocketMQTransactionListener
public class OperationTransactionListener implements TransactionChecker {
    private static final Logger log = LoggerFactory.getLogger(OperationTransactionListener.class);

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public TransactionResolution check(MessageView messageView) {
        try {


            // 1. 安全提取消息内容
            String messageBody = extractMessageBody(messageView);
            if (messageBody == null || messageBody.isEmpty()) {
                log.warn("Empty message body");
                return TransactionResolution.ROLLBACK;
            }

            // 2. 安全解析JSON
            Map<String, Object> messageMap = parseMessage(messageBody);
            if (messageMap == null) {
                return TransactionResolution.ROLLBACK;
            }

            // 3. 安全获取user_id
            Long userId = getUserId(messageMap);
            if (userId == null) {
                return TransactionResolution.ROLLBACK;
            }

            // 4. 检查事务状态
            log.info("Checking transaction for userId: {}", userId);
            return checkUserTransaction(userId);
        } catch (Exception e) {
            log.error("Transaction check failed", e);
            return TransactionResolution.ROLLBACK;
        }
    }

    private String extractMessageBody(MessageView messageView) {
        try {
            ByteBuffer byteBuffer = messageView.getBody();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Message body extraction failed", e);
            return null;
        }
    }

    private Map<String, Object> parseMessage(String messageBody) {
        try {
            return objectMapper.readValue(messageBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Message parsing failed. Body: {}", messageBody, e);
            return null;
        }
    }

    private Long getUserId(Map<String, Object> messageMap) {
        try {
            // 获取 headers 子 map
            Object headersObj = messageMap.get("headers");
            if (!(headersObj instanceof Map)) {
                log.warn("Missing or invalid 'headers' field");
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) headersObj;

            // 提取 KEYS 字段
            Object userIdObj = headers.get("KEYS");
            if (userIdObj == null) {
                String messageStr = objectMapper.writeValueAsString(messageMap);
                log.warn("Received message without user_id, raw message: {}", messageStr);
                return null;
            }

            return Long.parseLong(userIdObj.toString());
        } catch (Exception e) {
            log.error("Failed to extract user_id from headers", e);
            return null;
        }
    }


        private TransactionResolution checkUserTransaction(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            return user != null ?
                    TransactionResolution.COMMIT :
                    TransactionResolution.ROLLBACK;
        } catch (Exception e) {
            log.error("User check failed for userId: {}", userId, e);
            return TransactionResolution.ROLLBACK;
        }
    }

}
*/