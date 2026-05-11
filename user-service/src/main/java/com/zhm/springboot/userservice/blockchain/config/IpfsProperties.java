package com.zhm.springboot.userservice.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * IPFS 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blockchain.ipfs")
public class IpfsProperties {

    /**
     * IPFS 模式：pinata（Pinata 服务）、local（本地节点）、disabled（降级到 MinIO）
     */
    private String mode = "pinata";

    /**
     * IPFS 节点 URL（用于 local 模式上传）
     */
    private String url = "http://localhost:5001";

    /**
     * IPFS 网关 URL（用于访问文件）
     */
    private String gateway = "https://gateway.pinata.cloud/ipfs/";

    /**
     * 备用网关列表（用于故障转移）
     */
    private List<String> backupGateways = new ArrayList<>();

    /**
     * Pinata API URL（用于 pinata 模式）
     */
    private String pinataUrl = "https://api.pinata.cloud";

    /**
     * Pinata JWT Token（用于 pinata 模式认证）
     */
    private String pinataJwt = "";

    /**
     * 上传文件大小限制（默认 50MB）
     */
    private Long maxFileSize = 50 * 1024 * 1024L;

    /**
     * 批量上传最大文件数
     */
    private Integer maxBatchSize = 10;

    /**
     * 允许的文件类型
     */
    private List<String> allowedMimeTypes = new ArrayList<>();

    /**
     * 连接超时（秒）
     */
    private Integer connectTimeout = 60;

    /**
     * 写入超时（秒）
     */
    private Integer writeTimeout = 120;

    /**
     * 读取超时（秒）
     */
    private Integer readTimeout = 120;

    /**
     * 重试次数
     */
    private Integer retryCount = 3;

    /**
     * 是否自动固定文件
     */
    private Boolean autoPin = true;

    /**
     * 是否启用本地缓存
     */
    private Boolean enableCache = true;

    /**
     * 缓存目录
     */
    private String cacheDir = "./ipfs-cache";

    public IpfsProperties() {
        allowedMimeTypes.add("image/jpeg");
        allowedMimeTypes.add("image/png");
        allowedMimeTypes.add("image/gif");
        allowedMimeTypes.add("image/webp");
        allowedMimeTypes.add("image/svg+xml");
        allowedMimeTypes.add("application/json");
        allowedMimeTypes.add("text/plain");
        allowedMimeTypes.add("video/mp4");
        allowedMimeTypes.add("audio/mpeg");

        backupGateways.add("https://gateway.ipfs.io/ipfs/");
        backupGateways.add("https://cloudflare-ipfs.com/ipfs/");
        backupGateways.add("https://dweb.link/ipfs/");
    }

    /**
     * 判断是否使用 Pinata 模式
     */
    public boolean isPinataMode() {
        return "pinata".equalsIgnoreCase(mode);
    }

    /**
     * 判断是否使用本地 IPFS 节点模式
     */
    public boolean isLocalMode() {
        return "local".equalsIgnoreCase(mode);
    }

    /**
     * 判断 IPFS 是否已禁用（降级到 MinIO）
     */
    public boolean isDisabled() {
        return "disabled".equalsIgnoreCase(mode);
    }
}