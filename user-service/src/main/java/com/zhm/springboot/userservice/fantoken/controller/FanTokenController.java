package com.zhm.springboot.userservice.fantoken.controller;

import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.fantoken.entity.FanToken;
import com.zhm.springboot.userservice.fantoken.entity.StakeRecord;
import com.zhm.springboot.userservice.fantoken.service.FanTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * 粉丝代币控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/fan-token")
@RequiredArgsConstructor
public class FanTokenController {

    private final FanTokenService fanTokenService;

    /**
     * 创建粉丝代币
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createFanToken(
            @RequestParam Long projectId,
            @RequestParam String creatorAddress,
            @RequestParam String name,
            @RequestParam String symbol,
            @RequestParam String totalSupply) {
        log.info("收到创建粉丝代币请求：projectId={}, creatorAddress={}, name={}, symbol={}",
                projectId, creatorAddress, name, symbol);

        Long id = fanTokenService.createFanToken(
                projectId, creatorAddress, name, symbol, new BigInteger(totalSupply)
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    /**
     * 配置公募
     */
    @PostMapping("/sale")
    public ResponseEntity<ApiResponse<Void>> configurePublicSale(
            @RequestParam String tokenAddress,
            @RequestParam String price,
            @RequestParam Long startTime,
            @RequestParam Long endTime) {
        log.info("收到配置公募请求：tokenAddress={}, price={}, startTime={}, endTime={}",
                tokenAddress, price, startTime, endTime);

        fanTokenService.configurePublicSale(
                tokenAddress, new BigInteger(price), startTime, endTime
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 参与公募
     */
    @PostMapping("/participate")
    public ResponseEntity<ApiResponse<Void>> participateInSale(
            @RequestParam String tokenAddress,
            @RequestParam String buyerAddress,
            @RequestParam String amount,
            @RequestParam String paymentAmount) {
        log.info("收到参与公募请求：tokenAddress={}, buyerAddress={}, amount={}, paymentAmount={}",
                tokenAddress, buyerAddress, amount, paymentAmount);

        fanTokenService.participateInSale(
                tokenAddress, buyerAddress,
                new BigInteger(amount), new BigInteger(paymentAmount)
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 质押代币
     */
    @PostMapping("/stake")
    public ResponseEntity<ApiResponse<Void>> stake(
            @RequestParam String userAddress,
            @RequestParam String tokenAddress,
            @RequestParam String amount) {
        log.info("收到质押请求：userAddress={}, tokenAddress={}, amount={}",
                userAddress, tokenAddress, amount);

        fanTokenService.stake(userAddress, tokenAddress, new BigInteger(amount));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 解除质押
     */
    @PostMapping("/unstake")
    public ResponseEntity<ApiResponse<Void>> unstake(
            @RequestParam String userAddress,
            @RequestParam String tokenAddress,
            @RequestParam String amount) {
        log.info("收到解除质押请求：userAddress={}, tokenAddress={}, amount={}",
                userAddress, tokenAddress, amount);

        fanTokenService.unstake(userAddress, tokenAddress, new BigInteger(amount));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 领取奖励
     */
    @PostMapping("/reward")
    public ResponseEntity<ApiResponse<String>> claimReward(
            @RequestParam String userAddress,
            @RequestParam String tokenAddress) {
        log.info("收到领取奖励请求：userAddress={}, tokenAddress={}", userAddress, tokenAddress);

        BigInteger reward = fanTokenService.claimReward(userAddress, tokenAddress);
        return ResponseEntity.ok(ApiResponse.success(reward.toString()));
    }

    /**
     * 获取粉丝代币信息
     */
    @GetMapping("/{tokenAddress}")
    public ResponseEntity<ApiResponse<FanToken>> getFanToken(@PathVariable String tokenAddress) {
        FanToken fanToken = fanTokenService.getFanTokenByAddress(tokenAddress);
        return ResponseEntity.ok(ApiResponse.success(fanToken));
    }

    /**
     * 获取用户的粉丝代币
     */
    @GetMapping("/creator/{address}")
    public ResponseEntity<ApiResponse<List<FanToken>>> getFanTokensByCreator(@PathVariable String address) {
        List<FanToken> tokens = fanTokenService.getFanTokensByCreator(address);
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }

    /**
     * 获取质押记录
     */
    @GetMapping("/stake-record")
    public ResponseEntity<ApiResponse<StakeRecord>> getStakeRecord(
            @RequestParam String userAddress,
            @RequestParam String tokenAddress) {
        StakeRecord record = fanTokenService.getStakeRecord(userAddress, tokenAddress);
        return ResponseEntity.ok(ApiResponse.success(record));
    }
}