package com.zhm.springboot.userservice.market.controller;

import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.market.entity.MarketOrder;
import com.zhm.springboot.userservice.market.service.MarketService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * 市场交易控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    /**
     * 创建固定价格销售订单
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<String>> createFixedPriceSale(
            @RequestParam @NotBlank @Pattern(regexp = "^0x[0-9a-fA-F]{40}$", message = "卖家地址格式不正确") String sellerAddress,
            @RequestParam @NotBlank @Pattern(regexp = "^0x[0-9a-fA-F]{40}$", message = "合约地址格式不正确") String nftContract,
            @RequestParam Long tokenId,
            @RequestParam @NotBlank String price,
            @RequestParam(required = false) Long nftAssetId,
            @RequestParam(required = false) String paymentToken,
            @RequestParam(required = false) Long endTime) throws Exception {
        log.info("收到固定价格挂单请求：sellerAddress={}, tokenId={}, price={}, nftAssetId={}",
                sellerAddress, tokenId, price, nftAssetId);

        String orderId = marketService.createFixedPriceSale(
                sellerAddress, nftContract, tokenId,
                new BigInteger(price), nftAssetId, paymentToken, endTime
        );
        return ResponseEntity.ok(ApiResponse.success(orderId));
    }

    /**
     * 创建荷兰拍卖订单
     */
    @PostMapping("/auction")
    public ResponseEntity<ApiResponse<String>> createDutchAuction(
            @RequestParam String sellerAddress,
            @RequestParam String nftContract,
            @RequestParam Long tokenId,
            @RequestParam String startPrice,
            @RequestParam String reservePrice,
            @RequestParam Long startTime,
            @RequestParam Long duration) throws Exception {
        log.info("收到荷兰拍卖请求：sellerAddress={}, tokenId={}, startPrice={}, reservePrice={}",
                sellerAddress, tokenId, startPrice, reservePrice);

        String orderId = marketService.createDutchAuction(
                sellerAddress, nftContract, tokenId,
                new BigInteger(startPrice), new BigInteger(reservePrice),
                startTime, duration
        );
        return ResponseEntity.ok(ApiResponse.success(orderId));
    }

    /**
     * 购买 NFT
     */
    @PostMapping("/buy/{orderId}")
    public ResponseEntity<ApiResponse<String>> buyNft(
            @PathVariable String orderId,
            @RequestParam @NotBlank @Pattern(regexp = "^0x[0-9a-fA-F]{40}$", message = "买家地址格式不正确") String buyerAddress) throws Exception {
        log.info("收到购买请求：orderId={}, buyerAddress={}", orderId, buyerAddress);

        String txHash = marketService.buyNft(orderId, buyerAddress);
        return ResponseEntity.ok(ApiResponse.success(txHash));
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String sellerAddress) throws Exception {
        log.info("收到取消订单请求：orderId={}, sellerAddress={}", orderId, sellerAddress);

        String txHash = marketService.cancelOrder(orderId, sellerAddress);
        return ResponseEntity.ok(ApiResponse.success(txHash));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<ApiResponse<MarketOrder>> getOrder(@PathVariable String id) {
        MarketOrder order = marketService.getOrder(id);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<MarketOrder>>> getOrderList(
            @RequestParam(required = false) String sellerAddress,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<MarketOrder> orders = marketService.getOrderList(sellerAddress, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * 获取用户交易历史（作为买家或卖家）
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<MarketOrder>>> getUserHistory(
            @RequestParam String address,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MarketOrder> orders = marketService.getUserHistory(address, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}
