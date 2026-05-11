package com.zhm.springboot.userservice.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhm.springboot.userservice.entity.User;
import groovy.util.logging.Slf4j;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import lombok.SneakyThrows;
import org.apache.rocketmq.client.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OperationLogProducer{

    @Autowired
    private RocketMQClientTemplate rocketMQClientTemplate;

    @Value("${rocketmq.topic.operation-log}")
    private String operationLogTopic;

    /**在网上和ai学的发送普通消息*/
    public void sendOperationLog(Long userId, String action, String ip, String detail) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("user_id", userId);
        logMap.put("action", action);
        logMap.put("ip", ip);
        logMap.put("detail", detail);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(logMap);

            Message<byte[]> message = MessageBuilder.withPayload(
                    json.getBytes(StandardCharsets.UTF_8)
            ).build();
            rocketMQClientTemplate.send(operationLogTopic, message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    /** 由ai指导发送事务消息，虽然成功发送到了Rocketmq上面，但消费不了，故暂且搁置
//    @SneakyThrows
//    public void sendOperationLog(Long userId, String action, String ip, String detail) {
//
//        Map<String, Object> logMap = new HashMap<>();
//        logMap.put("user_id", userId);
//        logMap.put("action", action);
//        logMap.put("ip", ip);
//        logMap.put("detail", detail);
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            String json = objectMapper.writeValueAsString(logMap);
//
//            Message<byte[]>
//                    message = MessageBuilder.withPayload(
//                    json.getBytes(StandardCharsets.UTF_8)
//            )
//                    .setHeader(RocketMQHeaders.KEYS, userId.toString())
//                    .build();
//
//             rocketMQClientTemplate.sendMessageInTransaction(operationLogTopic, message);
//        } catch (JsonProcessingException e) {
//            // 处理异常
//            e.printStackTrace();
//        }
//    }

    /*
     * 发送用户注册日志
     */
    public void sendRegisterLog(User user, String ip) {
        sendOperationLog(
                user.getUserId(),
                "register",
                ip,
                "新用户注册: " + user.getUsername()
        );
    }

// 发送用户更新日志，重复机械编码使用ai工具自动化
    public void sendUpdateLog(Long userId,
                              Long targetUserId,
                              User oldUser,
                              User newUser,
                              String ip) {
        Map<String, Map<String, String>> changes = new HashMap<>();


        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            changes.put("email", Map.of(
                    "old", oldUser.getEmail(),
                    "new", newUser.getEmail()
            ));
        }

        if (!oldUser.getPhone().equals(newUser.getPhone())) {
            changes.put("phone", Map.of(
                    "old", oldUser.getPhone(),
                    "new", newUser.getPhone()
            ));
        }
        if (!oldUser.getGmtCreate().equals(newUser.getGmtCreate()))
            changes.put("gmt_create", Map.of(
                    "old", oldUser.getGmtCreate().toString(),
                    "new", newUser.getGmtCreate().toString()
            ));

        sendOperationLog(
                userId,
                "update_user",
                ip,
                "更新用户ID[" + targetUserId + "]信息: " + changes.toString()
        );

    }

}
