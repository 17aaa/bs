package com.zhm.springboot.userservice.controller;

import com.zhm.springboot.userservice.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供应用状态信息
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final Environment environment;

    /**
     * 基础健康检查
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(status);
    }

    /**
     * 应用信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("app", "CreativeNFT User Service");
        info.put("version", "1.0.0");
        info.put("profiles", environment.getActiveProfiles());
        info.put("timestamp", LocalDateTime.now().toString());
        return ApiResponse.success(info);
    }
}
