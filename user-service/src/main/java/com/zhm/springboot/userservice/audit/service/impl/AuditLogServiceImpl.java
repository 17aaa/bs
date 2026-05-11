package com.zhm.springboot.userservice.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhm.springboot.userservice.audit.entity.AuditLog;
import com.zhm.springboot.userservice.audit.mapper.AuditLogMapper;
import com.zhm.springboot.userservice.audit.service.AuditLogService;
import com.zhm.springboot.userservice.audit.vo.AuditLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

    private static final Map<String, String> ACTION_LABELS = new HashMap<>();
    private static final Map<String, String> MODULE_LABELS = new HashMap<>();

    static {
        ACTION_LABELS.put(AuditLog.ACTION_CREATE, "创建");
        ACTION_LABELS.put(AuditLog.ACTION_UPDATE, "更新");
        ACTION_LABELS.put(AuditLog.ACTION_DELETE, "删除");
        ACTION_LABELS.put(AuditLog.ACTION_LOGIN, "登录");
        ACTION_LABELS.put(AuditLog.ACTION_LOGOUT, "登出");
        ACTION_LABELS.put(AuditLog.ACTION_TRANSFER, "转移");
        ACTION_LABELS.put(AuditLog.ACTION_MINT, "铸造");
        ACTION_LABELS.put(AuditLog.ACTION_BUY, "购买");
        ACTION_LABELS.put(AuditLog.ACTION_SELL, "出售");
        ACTION_LABELS.put(AuditLog.ACTION_APPROVE, "审核通过");
        ACTION_LABELS.put(AuditLog.ACTION_REJECT, "审核拒绝");
        ACTION_LABELS.put(AuditLog.ACTION_BAN, "封禁");
        ACTION_LABELS.put(AuditLog.ACTION_UNBAN, "解封");

        MODULE_LABELS.put(AuditLog.MODULE_USER, "用户管理");
        MODULE_LABELS.put(AuditLog.MODULE_NFT, "NFT管理");
        MODULE_LABELS.put(AuditLog.MODULE_MARKET, "市场交易");
        MODULE_LABELS.put(AuditLog.MODULE_AUTH, "认证授权");
        MODULE_LABELS.put(AuditLog.MODULE_ADMIN, "系统管理");
        MODULE_LABELS.put(AuditLog.MODULE_WALLET, "钱包管理");
    }

    @Override
    @Async
    public void log(Long userId, String username, String action, String module, String description,
                    String resourceType, String resourceId, HttpServletRequest request) {
        logSuccess(userId, username, action, module, description, resourceType, resourceId, request);
    }

    @Override
    @Async
    public void log(Long userId, String action, String module, String description) {
        logSuccess(userId, null, action, module, description, null, null, null);
    }

    @Override
    @Async
    public void logSuccess(Long userId, String username, String action, String module, String description,
                           String resourceType, String resourceId, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setModule(module);
        auditLog.setDescription(description);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setStatus("success");
        auditLog.setCreatedAt(LocalDateTime.now());

        if (request != null) {
            auditLog.setIpAddress(getClientIp(request));
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setRequestPath(request.getRequestURI());
        }

        auditLogMapper.insert(auditLog);
        log.info("操作日志: userId={}, action={}, module={}, description={}", userId, action, module, description);
    }

    @Override
    @Async
    public void logFailed(Long userId, String username, String action, String module, String description,
                          String resourceType, String resourceId, String errorMessage, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setModule(module);
        auditLog.setDescription(description);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setStatus("failed");
        auditLog.setErrorMessage(errorMessage);
        auditLog.setCreatedAt(LocalDateTime.now());

        if (request != null) {
            auditLog.setIpAddress(getClientIp(request));
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setRequestPath(request.getRequestURI());
        }

        auditLogMapper.insert(auditLog);
        log.warn("操作失败日志: userId={}, action={}, module={}, error={}", userId, action, module, errorMessage);
    }

    @Override
    public List<AuditLogVO> getUserLogs(Long userId, int page, int size) {
        int offset = (page - 1) * size;

        QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("created_at")
               .last("LIMIT " + size + " OFFSET " + offset);

        List<AuditLog> logs = auditLogMapper.selectList(wrapper);
        return convertToVOList(logs);
    }

    @Override
    public List<AuditLogVO> getAllLogs(int page, int size, String module, String action) {
        int offset = (page - 1) * size;

        QueryWrapper<AuditLog> wrapper = new QueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.eq("module", module);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.eq("action", action);
        }
        wrapper.orderByDesc("created_at")
               .last("LIMIT " + size + " OFFSET " + offset);

        List<AuditLog> logs = auditLogMapper.selectList(wrapper);
        return convertToVOList(logs);
    }

    @Override
    public AuditLog getLog(Long id) {
        return auditLogMapper.selectById(id);
    }

    private List<AuditLogVO> convertToVOList(List<AuditLog> logs) {
        List<AuditLogVO> vos = new ArrayList<>();
        for (AuditLog log : logs) {
            AuditLogVO vo = new AuditLogVO();
            vo.setId(log.getId());
            vo.setUserId(log.getUserId());
            vo.setUsername(log.getUsername());
            vo.setAction(log.getAction());
            vo.setActionLabel(ACTION_LABELS.getOrDefault(log.getAction(), log.getAction()));
            vo.setModule(log.getModule());
            vo.setModuleLabel(MODULE_LABELS.getOrDefault(log.getModule(), log.getModule()));
            vo.setDescription(log.getDescription());
            vo.setResourceType(log.getResourceType());
            vo.setResourceId(log.getResourceId());
            vo.setIpAddress(log.getIpAddress());
            vo.setRequestMethod(log.getRequestMethod());
            vo.setRequestPath(log.getRequestPath());
            vo.setStatus(log.getStatus());
            vo.setErrorMessage(log.getErrorMessage());
            vo.setCreatedAt(log.getCreatedAt());
            vos.add(vo);
        }
        return vos;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}