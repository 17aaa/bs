package com.zhm.springboot.userservice.config;

import com.zhm.springboot.userservice.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**", "/user/**", "/admin/**")
                .excludePathPatterns(
                        "/api/upload/**",           // 文件上传不限流
                        "/api/nft/mint-task/**",    // 铸造任务轮询不限流
                        "/actuator/**"              // 健康检查不限流
                );
    }
}