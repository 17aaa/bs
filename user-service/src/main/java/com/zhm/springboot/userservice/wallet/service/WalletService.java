package com.zhm.springboot.userservice.wallet.service;

import com.zhm.springboot.userservice.blockchain.security.WalletCredentialService;
import com.zhm.springboot.userservice.wallet.entity.Wallet;
import com.zhm.springboot.userservice.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 钱包服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper;
    private final WalletCredentialService walletCredentialService;

    /**
     * 为用户创建钱包
     */
    @Transactional
    public Wallet createWalletForUser(Long userId, String password) {
        log.info("为用户创建钱包：userId={}", userId);

        // 检查用户是否已有钱包
        Wallet existingWallet = walletMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Wallet>()
                .eq("user_id", userId)
        );

        if (existingWallet != null) {
            log.info("用户已有钱包：userId={}, address={}", userId, existingWallet.getWalletAddress());
            return existingWallet;
        }

        // 创建新钱包
        WalletCredentialService.WalletCredentials credentials =
            walletCredentialService.createWallet(password);

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setWalletAddress(credentials.getAddress());
        wallet.setKeystorePath(credentials.getKeystorePath());
        wallet.setIsBound(true);
        wallet.setBoundAt(LocalDateTime.now());
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        walletMapper.insert(wallet);
        log.info("为用户创建钱包成功：userId={}, address={}", userId, credentials.getAddress());

        return wallet;
    }

    /**
     * 获取用户钱包
     */
    public Wallet getUserWallet(Long userId) {
        log.debug("获取用户钱包：userId={}", userId);
        return walletMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Wallet>()
                .eq("user_id", userId)
                .eq("is_bound", true)
        );
    }

    /**
     * 获取或创建用户钱包
     */
    @Transactional
    public Wallet getOrCreateWallet(Long userId, String password) {
        Wallet wallet = getUserWallet(userId);
        if (wallet == null) {
            log.info("用户钱包不存在，创建新钱包：userId={}", userId);
            return createWalletForUser(userId, password);
        }
        return wallet;
    }

    /**
     * 绑定现有钱包地址
     */
    @Transactional
    public Wallet bindWalletAddress(Long userId, String walletAddress) {
        log.info("绑定钱包地址：userId={}, address={}", userId, walletAddress);

        // 检查是否已绑定
        Wallet existingWallet = walletMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Wallet>()
                .eq("user_id", userId)
        );

        if (existingWallet != null) {
            existingWallet.setWalletAddress(walletAddress);
            existingWallet.setIsBound(true);
            existingWallet.setBoundAt(LocalDateTime.now());
            existingWallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateById(existingWallet);
            log.info("更新用户钱包地址：userId={}, address={}", userId, walletAddress);
            return existingWallet;
        }

        // 创建新记录
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setWalletAddress(walletAddress);
        wallet.setIsBound(true);
        wallet.setBoundAt(LocalDateTime.now());
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        walletMapper.insert(wallet);
        log.info("创建用户钱包记录：userId={}, address={}", userId, walletAddress);

        return wallet;
    }
}