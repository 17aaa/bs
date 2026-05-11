package com.zhm.springboot.userservice.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 合约地址配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blockchain.contract")
public class ContractProperties {

    /**
     * NFT Asset 合约地址
     */
    private String nftAssetAddress = "";

    /**
     * Micro Market 合约地址
     */
    private String microMarketAddress = "";

    /**
     * Fan Token 合约地址
     */
    private String fanTokenAddress = "";

    /**
     * 检查合约地址是否已配置
     */
    public boolean isNftAssetConfigured() {
        return nftAssetAddress != null && !nftAssetAddress.isEmpty();
    }

    public boolean isMicroMarketConfigured() {
        return microMarketAddress != null && !microMarketAddress.isEmpty();
    }

    public boolean isFanTokenConfigured() {
        return fanTokenAddress != null && !fanTokenAddress.isEmpty();
    }
}