package com.zhm.springboot.userservice.blockchain.service;

import com.zhm.springboot.userservice.blockchain.config.TransactionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 交易管理服务
 * 处理 Nonce 管理、交易签名、发送和确认
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionProperties transactionProperties;
    private final Web3j web3j;

    // Nonce 缓存：address -> nonce
    private final ConcurrentHashMap<String, AtomicLong> nonceCache = new ConcurrentHashMap<>();

    /**
     * 获取下一个 Nonce（从链上获取）
     */
    public BigInteger getNextNonce(String address) throws Exception {
        // 先从本地缓存获取，避免频繁请求链上数据
        AtomicLong cachedNonce = nonceCache.get(address);
        if (cachedNonce != null) {
            return BigInteger.valueOf(cachedNonce.getAndIncrement());
        }

        // 从链上获取当前 Nonce
        EthGetTransactionCount txCount = web3j.ethGetTransactionCount(
                address,
                DefaultBlockParameterName.PENDING
        ).send();
        BigInteger nonce = txCount.getTransactionCount();

        // 初始化缓存
        nonceCache.computeIfAbsent(address, k -> new AtomicLong(nonce.longValue()));

        log.info("Got nonce {} for address {}", nonce, address);
        return nonce;
    }

    /**
     * 发送交易（使用 Web3j）
     */
    public String sendTransaction(Credentials credentials, String to, BigInteger value, String data) throws Exception {
        log.info("Sending transaction to: {}", to);

        String fromAddress = credentials.getAddress();
        BigInteger nonce = getNextNonce(fromAddress);
        BigInteger gasPrice = getCurrentGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(500000); // 默认 Gas Limit

        // 创建交易
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value,
                data
        );

        // 签名交易
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String signedMessageHex = Numeric.toHexString(signedMessage);

        // 发送交易
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedMessageHex).send();

        if (ethSendTransaction.hasError()) {
            String errorMsg = ethSendTransaction.getError().getMessage();
            log.error("Transaction failed: {}", errorMsg);
            throw new Exception("Transaction failed: " + errorMsg);
        }

        String txHash = ethSendTransaction.getTransactionHash();
        log.info("Transaction sent: {}", txHash);

        // 更新本地 Nonce 缓存
        AtomicLong cachedNonce = nonceCache.get(fromAddress);
        if (cachedNonce != null) {
            cachedNonce.incrementAndGet();
        }

        return txHash;
    }

    /**
     * 发送交易（带重试）
     */
    public String sendTransactionWithRetry(Credentials credentials, String to, BigInteger value, String data) throws Exception {
        int retries = 0;
        Exception lastException = null;

        while (retries < transactionProperties.getMaxRetries()) {
            try {
                return sendTransaction(credentials, to, value, data);
            } catch (Exception e) {
                lastException = e;
                retries++;
                log.warn("Transaction failed (attempt {}/{}): {}", retries,
                        transactionProperties.getMaxRetries(), e.getMessage());

                if (retries < transactionProperties.getMaxRetries()) {
                    Thread.sleep(transactionProperties.getRetryInterval());
                }
            }
        }

        throw new RuntimeException("Transaction failed after " + retries + " retries", lastException);
    }

    /**
     * 等待交易确认（轮询交易回执）
     */
    public TransactionReceipt waitForTransactionReceipt(String txHash) throws Exception {
        log.info("Waiting for transaction confirmation: {}", txHash);

        int maxAttempts = transactionProperties.getMaxRetries();
        long sleepInterval = transactionProperties.getRetryInterval();

        for (int i = 0; i < maxAttempts; i++) {
            try {
                TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send()
                        .getResult();

                if (receipt != null) {
                    if (receipt.isStatusOK()) {
                        log.info("Transaction confirmed: {}", txHash);
                    } else {
                        log.error("Transaction failed: {}", txHash);
                        throw new Exception("Transaction reverted: " + txHash);
                    }
                    return receipt;
                }

                log.debug("Waiting for receipt (attempt {}/{})", i + 1, maxAttempts);
                Thread.sleep(sleepInterval);

            } catch (Exception e) {
                log.error("Error getting receipt: {}", e.getMessage());
                throw e;
            }
        }

        throw new Exception("Transaction receipt not found after " + maxAttempts + " attempts");
    }

    /**
     * 发送交易并等待确认
     */
    public TransactionReceipt sendAndWait(Credentials credentials, String to, BigInteger value, String data) throws Exception {
        String txHash = sendTransactionWithRetry(credentials, to, value, data);
        return waitForTransactionReceipt(txHash);
    }

    /**
     * 获取当前 Gas 价格（从链上获取）
     */
    public BigInteger getCurrentGasPrice() throws Exception {
        try {
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            log.info("Current gas price: {} wei", gasPrice);
            return gasPrice != null ? gasPrice : BigInteger.valueOf(30000000000L);
        } catch (Exception e) {
            log.warn("Failed to get gas price, using default: 30 Gwei");
            return BigInteger.valueOf(30000000000L); // 30 Gwei
        }
    }
}