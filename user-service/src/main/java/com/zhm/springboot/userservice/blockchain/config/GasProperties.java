package com.zhm.springboot.userservice.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * Gas 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blockchain.gas")
public class GasProperties {

    /**
     * Gas 价格（Wei）
     */
    private BigInteger price = BigInteger.valueOf(30000000000L);

    /**
     * Gas 限制
     */
    private BigInteger limit = BigInteger.valueOf(3000000L);

    /**
     * Gas 缓冲系数（1.2 = 20% 缓冲）
     */
    private Double bufferFactor = 1.2;

    /**
     * 获取应用缓冲后的 Gas 限制
     */
    public BigInteger getBufferedLimit() {
        return limit.multiply(BigInteger.valueOf(bufferFactor.longValue()));
    }
}