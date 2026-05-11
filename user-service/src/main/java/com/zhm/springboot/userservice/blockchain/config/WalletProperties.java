package com.zhm.springboot.userservice.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 钱包配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blockchain.wallet")
public class WalletProperties {

    /**
     * 平台钱包私钥（用于部署合约和平台操作）
     */
    private String platformPrivateKey = "";

    /**
     * Keystore 存储路径
     */
    private String keystorePath = "./wallets/keystore";

    /**
     * 加密密码（从环境变量读取）
     */
    private String encryptionPassword = "";
}