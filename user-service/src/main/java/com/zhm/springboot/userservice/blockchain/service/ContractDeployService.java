package com.zhm.springboot.userservice.blockchain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 智能合约部署服务
 * 提供合约部署辅助功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractDeployService {

    private final Web3j web3j;
    private final ExecutorService blockchainExecutor;

    @Value("${blockchain.platform-private-key:}")
    private String platformPrivateKey;

    @Value("${blockchain.gas-price:30000000000}")
    private long gasPrice;

    @Value("${blockchain.gas-limit:500000}")
    private long gasLimit;

    private final ContractGasProvider gasProvider = new DefaultGasProvider();

    /**
     * 获取平台凭证
     */
    public Credentials getPlatformCredentials() {
        return Credentials.create(platformPrivateKey);
    }

    /**
     * 获取 Gas Provider
     */
    public ContractGasProvider getGasProvider() {
        return gasProvider;
    }

    /**
     * 获取交易 Nonce
     */
    public BigInteger getNonce(String address) throws Exception {
        EthGetTransactionCount txCount = web3j.ethGetTransactionCount(
                address,
                DefaultBlockParameterName.PENDING
        ).send();
        return txCount.getTransactionCount();
    }

    /**
     * 等待交易回执
     */
    public TransactionReceipt waitForTransactionReceipt(String txHash) throws Exception {
        return web3j.ethGetTransactionReceipt(txHash).send()
                .getTransactionReceipt().orElseThrow(() ->
                        new RuntimeException("交易回执不存在：" + txHash));
    }

    /**
     * 获取 Gas 价格
     */
    public BigInteger getGasPrice() {
        return BigInteger.valueOf(gasPrice);
    }

    /**
     * 获取 Gas 限制
     */
    public BigInteger getGasLimit() {
        return BigInteger.valueOf(gasLimit);
    }
}