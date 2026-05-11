package com.zhm.springboot.userservice.blockchain.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web3j.protocol.Web3j;
import org.springframework.web3j.protocol.http.HttpService;
import org.web3j.protocol.Web3jService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

/**
 * Web3j 配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class Web3jConfig {

    private final PolygonNetworkProperties networkProperties;

    /**
     * 创建 Web3j Bean
     */
    @Bean
    public Web3j web3j() {
        String rpcUrl = networkProperties.getRpcUrl();
        log.info("Initializing Web3j with RPC URL: {}", rpcUrl);

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));

        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("Connected to blockchain client: {}", clientVersion);

            // 获取当前区块高度
            BigInteger blockNumber = web3j.ethBlockNumber().send()
                    .getBlockNumber();
            log.info("Current block number: {}", blockNumber);
        } catch (Exception e) {
            log.warn("Failed to connect to blockchain: {}", e.getMessage());
        }

        return web3j;
    }

    /**
     * 合约 Gas Provider
     * 为合约调用提供 Gas 配置
     */
    @Bean
    public ContractGasProvider contractGasProvider() {
        return new DefaultGasProvider();
    }
}