package com.zhm.springboot.userservice.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private String code;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return code;
    }

    // 常用错误码
    public static final String CODE_NOT_FOUND = "NOT_FOUND";
    public static final String CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String CODE_FORBIDDEN = "FORBIDDEN";
    public static final String CODE_INVALID_PARAM = "INVALID_PARAM";
    public static final String CODE_DUPLICATE = "DUPLICATE";
    public static final String CODE_LIMIT_EXCEEDED = "LIMIT_EXCEEDED";

    // 静态工厂方法
    public static BusinessException notFound(String message) {
        return new BusinessException(CODE_NOT_FOUND, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(CODE_UNAUTHORIZED, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(CODE_FORBIDDEN, message);
    }

    public static BusinessException invalidParam(String message) {
        return new BusinessException(CODE_INVALID_PARAM, message);
    }

    public static BusinessException duplicate(String message) {
        return new BusinessException(CODE_DUPLICATE, message);
    }

    public static BusinessException limitExceeded(String message) {
        return new BusinessException(CODE_LIMIT_EXCEEDED, message);
    }
}