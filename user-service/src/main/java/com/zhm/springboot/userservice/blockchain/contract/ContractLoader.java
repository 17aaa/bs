package com.zhm.springboot.userservice.blockchain.contract;

import com.zhm.springboot.userservice.blockchain.config.ContractProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 合约加载器
 * 用于管理合约地址和状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractLoader {

    private final ContractProperties contractProperties;

    @PostConstruct
    public void init() {
        log.info("Contract loader initialized");
        log.info("NFT Asset Address: {}", contractProperties.getNftAssetAddress());
        log.info("Micro Market Address: {}", contractProperties.getMicroMarketAddress());
        log.info("Fan Token Address: {}", contractProperties.getFanTokenAddress());
    }

    /**
     * 获取 NFT Asset 合约地址
     */
    public String getNftAssetAddress() {
        return contractProperties.getNftAssetAddress();
    }

    /**
     * 获取 Micro Market 合约地址
     */
    public String getMicroMarketAddress() {
        return contractProperties.getMicroMarketAddress();
    }

    /**
     * 获取 Fan Token 合约地址
     */
    public String getFanTokenAddress() {
        return contractProperties.getFanTokenAddress();
    }

    /**
     * 检查合约地址是否有效
     */
    public boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        return address.matches("^0x[a-fA-F0-9]{40}$");
    }

    /**
     * 检查所有合约是否已部署
     */
    public boolean areAllContractsDeployed() {
        return isValidAddress(getNftAssetAddress()) &&
               isValidAddress(getMicroMarketAddress()) &&
               isValidAddress(getFanTokenAddress());
    }
}