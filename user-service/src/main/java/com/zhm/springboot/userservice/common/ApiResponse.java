package com.zhm.springboot.userservice.common;

import lombok.Data;

/**
 * 统一 API 响应类
 */
@Data
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }
}