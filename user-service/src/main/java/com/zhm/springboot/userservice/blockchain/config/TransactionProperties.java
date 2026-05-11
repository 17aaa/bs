package com.zhm.springboot.userservice.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 交易配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blockchain.transaction")
public class TransactionProperties {

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 重试间隔（毫秒）
     */
    private Long retryInterval = 2000L;

    /**
     * 交易确认等待时间（毫秒）
     */
    private Long confirmationTimeout = 60000L;

    /**
     * Nonce 缓存过期时间（秒）
     */
    private Long nonceCacheExpire = 300L;
}