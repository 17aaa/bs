package com.zhm.springboot.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置
 * Spring Boot 自动支持 kebab-case 到 camelCase 的映射
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {

    /**
     * 是否启用MinIO
     */
    private boolean enabled = true;

    /**
     * MinIO 服务地址
     */
    private String endpoint = "http://localhost:9000";

    /**
     * Access Key
     */
    private String accessKey = "minioadmin";

    /**
     * Secret Key
     */
    private String secretKey = "minioadmin";

    /**
     * Bucket 名称
     */
    private String bucketName = "nft-assets";

    /**
     * 外部访问地址（用于生成文件访问URL）
     * 如果不配置，则使用 endpoint
     */
    private String publicUrl;

    /**
     * 连接超时时间（秒）
     */
    private int connectTimeout = 10;

    /**
     * 写入超时时间（秒）
     */
    private int writeTimeout = 60;

    /**
     * 读取超时时间（秒）
     */
    private int readTimeout = 60;

    /**
     * 获取公开访问URL
     */
    public String getPublicAccessUrl() {
        return publicUrl != null && !publicUrl.isEmpty() ? publicUrl : endpoint;
    }
}