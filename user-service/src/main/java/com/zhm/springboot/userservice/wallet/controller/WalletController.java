package com.zhm.springboot.userservice.wallet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhm.springboot.userservice.blockchain.security.WalletCredentialService;
import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.util.JwtUtil;
import com.zhm.springboot.userservice.wallet.entity.Wallet;
import com.zhm.springboot.userservice.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 钱包控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletCredentialService walletCredentialService;
    private final StringRedisTemplate redisTemplate;
    private final WalletMapper walletMapper;
    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 创建新钱包
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createWallet(
            @RequestParam String password) throws Exception {
        log.info("收到创建钱包请求");

        WalletCredentialService.WalletCredentials credentials =
                walletCredentialService.createWallet(password);

        Map<String, Object> result = new HashMap<>();
        result.put("address", credentials.getAddress());
        result.put("keystorePath", credentials.getKeystorePath());
        // 注意：助记词只在创建时返回一次，用户需要安全保存
        result.put("mnemonic", credentials.getMnemonic());

        log.info("Wallet created: {}", credentials.getAddress());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 验证助记词
     */
    @PostMapping("/validate-mnemonic")
    public ResponseEntity<ApiResponse<Boolean>> validateMnemonic(
            @RequestParam String mnemonic) throws Exception {
        log.info("收到验证助记词请求");
        boolean valid = walletCredentialService.validateMnemonic(mnemonic);
        return ResponseEntity.ok(ApiResponse.success(valid));
    }

    /**
     * 从助记词恢复钱包地址
     */
    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<Map<String, Object>>> restoreWallet(
            @RequestParam String mnemonic) {
        log.info("收到恢复钱包请求");

        if (!walletCredentialService.validateMnemonic(mnemonic)) {
            return ResponseEntity.ok(ApiResponse.error("助记词格式不正确"));
        }

        String privateKeyHex = walletCredentialService.restoreFromMnemonic(mnemonic);
        org.web3j.crypto.Credentials credentials = org.web3j.crypto.Credentials.create(privateKeyHex);
        String address = credentials.getAddress();

        Map<String, Object> result = new HashMap<>();
        result.put("address", address);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取待签名的 challenge nonce（第一步）
     * 客户端使用 MetaMask 对该 nonce 签名后，调用 /verify 接口验证
     */
    @GetMapping("/challenge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChallenge(
            @RequestParam String address) {
        log.info("生成签名 challenge，address={}", address);

        byte[] nonceBytes = new byte[32];
        secureRandom.nextBytes(nonceBytes);
        String nonce = Numeric.toHexString(nonceBytes);
        String message = "请签名以验证您的钱包所有权：" + nonce;

        // challenge 存入 Redis，5 分钟有效
        redisTemplate.opsForValue().set(
            "wallet:challenge:" + address.toLowerCase(), message,
            5, TimeUnit.MINUTES);

        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 验证签名（第二步）
     * 客户端使用 MetaMask 的 personal_sign 对 challenge 消息签名后调用此接口
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifySignature(
            @RequestParam String address,
            @RequestParam String signature) {
        log.info("收到验证签名请求，address={}", address);

        String storedMessage = redisTemplate.opsForValue()
                .get("wallet:challenge:" + address.toLowerCase());

        if (storedMessage == null) {
            log.warn("challenge 不存在或已过期，address={}", address);
            return ResponseEntity.ok(ApiResponse.success(false));
        }

        try {
            String recoveredAddress = recoverAddress(storedMessage, signature);
            boolean matched = address.equalsIgnoreCase(recoveredAddress);
            if (matched) {
                // 验证成功后删除 challenge，防止重放攻击
                redisTemplate.delete("wallet:challenge:" + address.toLowerCase());
            }
            log.info("签名验证结果：address={}, matched={}", address, matched);
            return ResponseEntity.ok(ApiResponse.success(matched));
        } catch (Exception e) {
            log.warn("签名验证失败：address={}, error={}", address, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(false));
        }
    }

    /**
     * MetaMask 钱包登录
     * 验证签名成功后，查找绑定该钱包的用户并返回 JWT token
     *
     * @param address   钱包地址
     * @param signature MetaMask personal_sign 签名
     * @return JWT token 及用户信息
     */
    @PostMapping("/wallet-login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> walletLogin(
            @RequestParam String address,
            @RequestParam String signature) {
        log.info("钱包登录请求，address={}", address);

        // 1. 从 Redis 取出 challenge 消息
        String storedMessage = redisTemplate.opsForValue()
                .get("wallet:challenge:" + address.toLowerCase());
        if (storedMessage == null) {
            return ResponseEntity.ok(ApiResponse.error("challenge 不存在或已过期，请重新获取"));
        }

        // 2. 验证签名
        String recoveredAddress;
        try {
            recoveredAddress = recoverAddress(storedMessage, signature);
        } catch (Exception e) {
            log.warn("签名验证异常：address={}, error={}", address, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("签名验证失败"));
        }

        if (!address.equalsIgnoreCase(recoveredAddress)) {
            return ResponseEntity.ok(ApiResponse.error("签名地址不匹配"));
        }

        // 3. 删除 challenge，防止重放
        redisTemplate.delete("wallet:challenge:" + address.toLowerCase());

        // 4. 查找绑定该钱包的用户
        LambdaQueryWrapper<Wallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wallet::getWalletAddress, address.toLowerCase())
               .eq(Wallet::getIsBound, true);
        Wallet wallet = walletMapper.selectOne(wrapper);

        if (wallet == null) {
            return ResponseEntity.ok(ApiResponse.error("未找到绑定该钱包的账号，请先使用用户名密码登录并绑定钱包"));
        }

        // 5. 生成 JWT token
        String token = jwtUtil.createToken(wallet.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(wallet.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", wallet.getUserId());
        result.put("walletAddress", address);

        log.info("钱包登录成功：userId={}, address={}", wallet.getUserId(), address);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 使用 Web3j 从签名中恢复地址
     * 兼容 MetaMask 的 personal_sign（添加了以太坊消息前缀）
     */
    private String recoverAddress(String message, String signature) {
        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        byte v = signatureBytes[64];
        if (v < 27) v += 27;

        Sign.SignatureData signatureData = new Sign.SignatureData(
                v,
                java.util.Arrays.copyOfRange(signatureBytes, 0, 32),
                java.util.Arrays.copyOfRange(signatureBytes, 32, 64)
        );

        try {
            BigInteger publicKey = Sign.signedPrefixedMessageToKey(message.getBytes(), signatureData);
            return "0x" + Keys.getAddress(publicKey);
        } catch (java.security.SignatureException e) {
            throw new RuntimeException("签名验证失败", e);
        }
    }
}
