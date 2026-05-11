package com.zhm.springboot.userservice.audit.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 使用方法：@LogOperation(value = "描述", action = "create", module = "nft")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    String action() default "create";

    /**
     * 操作模块
     */
    String module() default "system";
}