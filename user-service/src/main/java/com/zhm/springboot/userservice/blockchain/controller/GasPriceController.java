package com.zhm.springboot.userservice.blockchain.controller;

import com.zhm.springboot.userservice.blockchain.service.GasPriceService;
import com.zhm.springboot.userservice.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Gas价格API控制器
 */
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class GasPriceController {

    private final GasPriceService gasPriceService;

    /**
     * 获取当前Gas价格
     */
    @GetMapping("/gas-price")
    public ApiResponse<Map<String, Object>> getGasPrice() {
        BigInteger currentPrice = gasPriceService.getGasPrice();

        // 计算不同速度的Gas价格
        Map<String, Object> result = new HashMap<>();

        // 慢速 (0.8x)
        Map<String, Object> slow = new HashMap<>();
        slow.put("price", currentPrice.multiply(BigInteger.valueOf(8)).divide(BigInteger.valueOf(10)));
        slow.put("time", "~5分钟");
        result.put("slow", slow);

        // 标准 (1x)
        Map<String, Object> standard = new HashMap<>();
        standard.put("price", currentPrice);
        standard.put("time", "~2分钟");
        result.put("standard", standard);

        // 快速 (1.5x)
        Map<String, Object> fast = new HashMap<>();
        fast.put("price", currentPrice.multiply(BigInteger.valueOf(15)).divide(BigInteger.valueOf(10)));
        fast.put("time", "~30秒");
        result.put("fast", fast);

        return ApiResponse.success(result);
    }
}