package com.zhm.springboot.userservice.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志 VO
 */
@Data
public class AuditLogVO {

    private Long id;

    private Long userId;

    private String username;

    private String action;

    private String actionLabel;

    private String module;

    private String moduleLabel;

    private String description;

    private String resourceType;

    private String resourceId;

    private String ipAddress;

    private String requestMethod;

    private String requestPath;

    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;
}