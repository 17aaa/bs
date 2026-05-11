package com.zhm.springboot.userservice.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("audit_logs")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 关联资源类型
     */
    private String resourceType;

    /**
     * 关联资源ID
     */
    private String resourceId;

    /**
     * 请求IP
     */
    private String ipAddress;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 操作状态：success/failed
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 操作时间
     */
    private LocalDateTime createdAt;

    // 操作类型常量
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGOUT = "logout";
    public static final String ACTION_TRANSFER = "transfer";
    public static final String ACTION_MINT = "mint";
    public static final String ACTION_BUY = "buy";
    public static final String ACTION_SELL = "sell";
    public static final String ACTION_APPROVE = "approve";
    public static final String ACTION_REJECT = "reject";
    public static final String ACTION_BAN = "ban";
    public static final String ACTION_UNBAN = "unban";

    // 模块常量
    public static final String MODULE_USER = "user";
    public static final String MODULE_NFT = "nft";
    public static final String MODULE_MARKET = "market";
    public static final String MODULE_AUTH = "auth";
    public static final String MODULE_ADMIN = "admin";
    public static final String MODULE_WALLET = "wallet";
}