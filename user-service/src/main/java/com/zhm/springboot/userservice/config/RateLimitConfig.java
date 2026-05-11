package com.zhm.springboot.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流配置类 - 简单令牌桶实现
 */
@Slf4j
@Component
public class RateLimitConfig {

    // 限流配置
    private static final int IP_LIMIT_PER_MINUTE = 100;
    private static final int USER_LIMIT_PER_MINUTE = 60;

    // 基于IP的限流记录
    private final Map<String, RateLimitEntry> ipLimitMap = new ConcurrentHashMap<>();

    // 基于用户的限流记录
    private final Map<Long, RateLimitEntry> userLimitMap = new ConcurrentHashMap<>();

    /**
     * 限流条目
     */
    private static class RateLimitEntry {
        private final AtomicInteger count;
        private volatile long lastResetTime;

        public RateLimitEntry() {
            this.count = new AtomicInteger(0);
            this.lastResetTime = System.currentTimeMillis();
        }

        public boolean tryAcquire(int limit) {
            long now = System.currentTimeMillis();
            // 每分钟重置
            if (now - lastResetTime >= 60000) {
                synchronized (this) {
                    if (now - lastResetTime >= 60000) {
                        count.set(0);
                        lastResetTime = now;
                    }
                }
            }
            return count.incrementAndGet() <= limit;
        }
    }

    /**
     * 检查IP是否允许请求
     */
    public boolean allowRequestByIp(String ip) {
        RateLimitEntry entry = ipLimitMap.computeIfAbsent(ip, k -> new RateLimitEntry());
        boolean allowed = entry.tryAcquire(IP_LIMIT_PER_MINUTE);
        if (!allowed) {
            log.warn("IP限流触发: ip={}", ip);
        }
        return allowed;
    }

    /**
     * 检查用户是否允许请求
     */
    public boolean allowRequestByUser(Long userId) {
        if (userId == null) return true;
        RateLimitEntry entry = userLimitMap.computeIfAbsent(userId, k -> new RateLimitEntry());
        boolean allowed = entry.tryAcquire(USER_LIMIT_PER_MINUTE);
        if (!allowed) {
            log.warn("用户限流触发: userId={}", userId);
        }
        return allowed;
    }

    /**
     * 清理过期的记录
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        ipLimitMap.entrySet().removeIf(entry ->
            now - entry.getValue().lastResetTime > 120000);
        userLimitMap.entrySet().removeIf(entry ->
            now - entry.getValue().lastResetTime > 120000);
        log.debug("限流记录清理完成: ip={}, user={}", ipLimitMap.size(), userLimitMap.size());
    }
}