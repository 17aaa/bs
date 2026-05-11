package com.zhm.springboot.userservice.notification.controller;

import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.notification.service.TransactionNotificationService;
import com.zhm.springboot.userservice.notification.vo.NotificationVO;
import com.zhm.springboot.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 交易通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final TransactionNotificationService notificationService;
    private final JwtUtil jwtUtil;

    /**
     * 获取通知列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationVO>>> getNotifications(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = token != null ? jwtUtil.parseToken(token.replace("Bearer ", "")) : null;
            if (userId == null) return ResponseEntity.ok(ApiResponse.success(List.of()));
            List<NotificationVO> notifications = notificationService.getUserNotifications(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success(notifications));
        } catch (Exception e) {
            log.warn("获取通知列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = token != null ? jwtUtil.parseToken(token.replace("Bearer ", "")) : null;
            if (userId == null) return ResponseEntity.ok(ApiResponse.success(0L));
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.warn("获取未读数量失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(0L));
        }
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.error("标记已读失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("操作失败：" + e.getMessage()));
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = token != null ? jwtUtil.parseToken(token.replace("Bearer ", "")) : null;
            if (userId != null) notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.warn("标记全部已读失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(null));
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = token != null ? jwtUtil.parseToken(token.replace("Bearer ", "")) : null;
            if (userId != null) notificationService.deleteNotification(id, userId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            log.warn("删除通知失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(null));
        }
    }
}