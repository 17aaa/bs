package com.zhm.springboot.userservice.audit.controller;

import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.admin.annotation.SuperAdminOnly;
import com.zhm.springboot.userservice.audit.service.AuditLogService;
import com.zhm.springboot.userservice.audit.vo.AuditLogVO;
import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户的操作日志
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AuditLogVO>>> getMyLogs(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = jwtUtil.parseToken(token.replace("Bearer ", ""));
            List<AuditLogVO> logs = auditLogService.getUserLogs(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            log.error("获取操作日志失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有操作日志（管理员）
     */
    @AdminOnly
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogVO>>> getAllLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action) {
        try {
            List<AuditLogVO> logs = auditLogService.getAllLogs(page, size, module, action);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            log.error("获取操作日志失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 获取日志详情
     */
    @SuperAdminOnly
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogVO>> getLog(@PathVariable Long id) {
        try {
            var log = auditLogService.getLog(id);
            if (log == null) {
                return ResponseEntity.notFound().build();
            }
            AuditLogVO vo = new AuditLogVO();
            vo.setId(log.getId());
            vo.setUserId(log.getUserId());
            vo.setUsername(log.getUsername());
            vo.setAction(log.getAction());
            vo.setModule(log.getModule());
            vo.setDescription(log.getDescription());
            vo.setStatus(log.getStatus());
            vo.setCreatedAt(log.getCreatedAt());
            return ResponseEntity.ok(ApiResponse.success(vo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }
}