package com.zhm.springboot.userservice.blockchain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gas 价格服务
 * 动态获取和缓存 Gas 价格
 */
@Slf4j
@Service
public class GasPriceService {

    private final AtomicReference<BigInteger> cachedGasPrice = new AtomicReference<>();

    /**
     * 获取当前 Gas 价格（带缓存）
     */
    public BigInteger getGasPrice() {
        BigInteger price = cachedGasPrice.get();
        if (price == null) {
            price = BigInteger.valueOf(30000000000L); // 30 Gwei 默认
            cachedGasPrice.set(price);
        }
        return price;
    }

    /**
     * 定期刷新 Gas 价格（每 30 秒）
     */
    @Scheduled(fixedRate = 30000)
    public void refreshGasPrice() {
        BigInteger newPrice = BigInteger.valueOf(30000000000L); // 简化版本
        BigInteger oldPrice = cachedGasPrice.getAndSet(newPrice);

        if (oldPrice == null || !oldPrice.equals(newPrice)) {
            log.info("Gas price updated: {} -> {} Wei", oldPrice, newPrice);
        }
    }

    /**
     * 获取 Gas 限制
     */
    public BigInteger getGasLimit() {
        return BigInteger.valueOf(3000000L);
    }
}