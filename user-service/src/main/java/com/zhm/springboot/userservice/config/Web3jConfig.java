package com.zhm.springboot.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Web3j 配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class Web3jConfig {

    @Value("${blockchain.rpc-url:https://rpc-amoy.polygon.technology/}")
    private String blockchainRpcUrl;

    /**
     * Web3j Bean
     */
    @Bean
    public Web3j web3j() {
        log.info("Initializing Web3j with RPC URL: {}", blockchainRpcUrl);
        return Web3j.build(new HttpService(blockchainRpcUrl));
    }

    /**
     * 区块链操作线程池
     */
    @Bean
    public ExecutorService blockchainExecutor() {
        return Executors.newFixedThreadPool(10, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("blockchain-executor-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
    }
}