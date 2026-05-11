package com.zhm.springboot.userservice.audit.service;

import com.zhm.springboot.userservice.audit.entity.AuditLog;
import com.zhm.springboot.userservice.audit.vo.AuditLogVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface AuditLogService {

    /**
     * 记录操作日志
     */
    void log(Long userId, String username, String action, String module, String description,
             String resourceType, String resourceId, HttpServletRequest request);

    /**
     * 记录操作日志（简化版）
     */
    void log(Long userId, String action, String module, String description);

    /**
     * 记录成功操作
     */
    void logSuccess(Long userId, String username, String action, String module, String description,
                    String resourceType, String resourceId, HttpServletRequest request);

    /**
     * 记录失败操作
     */
    void logFailed(Long userId, String username, String action, String module, String description,
                   String resourceType, String resourceId, String errorMessage, HttpServletRequest request);

    /**
     * 获取用户操作日志
     */
    List<AuditLogVO> getUserLogs(Long userId, int page, int size);

    /**
     * 获取所有操作日志（管理员）
     */
    List<AuditLogVO> getAllLogs(int page, int size, String module, String action);

    /**
     * 获取日志详情
     */
    AuditLog getLog(Long id);
}