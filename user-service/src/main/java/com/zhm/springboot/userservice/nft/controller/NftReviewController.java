package com.zhm.springboot.userservice.nft.controller;

import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.nft.entity.NftReview;
import com.zhm.springboot.userservice.nft.service.NftReviewService;
import com.zhm.springboot.userservice.nft.vo.NftReviewVO;
import com.zhm.springboot.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NFT 内容审核控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/nft/review")
@RequiredArgsConstructor
public class NftReviewController {

    private final NftReviewService nftReviewService;
    private final JwtUtil jwtUtil;

    /**
     * 提交NFT进行审核
     */
    @PostMapping("/submit/{nftAssetId}")
    public ResponseEntity<ApiResponse<NftReview>> submitForReview(@PathVariable Long nftAssetId) {
        try {
            NftReview review = nftReviewService.submitForReview(nftAssetId);
            return ResponseEntity.ok(ApiResponse.success(review));
        } catch (Exception e) {
            log.error("提交审核失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("提交失败：" + e.getMessage()));
        }
    }

    /**
     * 获取待审核列表（管理员）
     */
    @AdminOnly
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<NftReviewVO>>> getPendingReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<NftReviewVO> reviews = nftReviewService.getPendingReviews(page, size);
            return ResponseEntity.ok(ApiResponse.success(reviews));
        } catch (Exception e) {
            log.error("获取待审核列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 获取待审核数量（管理员）
     */
    @AdminOnly
    @GetMapping("/pending/count")
    public ResponseEntity<ApiResponse<Long>> getPendingCount() {
        try {
            long count = nftReviewService.getPendingCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("获取待审核数量失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 审核通过（管理员）
     */
    @AdminOnly
    @PostMapping("/approve/{nftAssetId}")
    public ResponseEntity<ApiResponse<NftReview>> approve(
            @PathVariable Long nftAssetId,
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String comment) {
        try {
            Long reviewerId = jwtUtil.parseToken(token.replace("Bearer ", ""));
            NftReview review = nftReviewService.approve(nftAssetId, reviewerId, comment);
            return ResponseEntity.ok(ApiResponse.success(review));
        } catch (Exception e) {
            log.error("审核通过失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("审核失败：" + e.getMessage()));
        }
    }

    /**
     * 审核拒绝（管理员）
     */
    @AdminOnly
    @PostMapping("/reject/{nftAssetId}")
    public ResponseEntity<ApiResponse<NftReview>> reject(
            @PathVariable Long nftAssetId,
            @RequestHeader("Authorization") String token,
            @RequestParam String rejectReason,
            @RequestParam(required = false) String comment) {
        try {
            Long reviewerId = jwtUtil.parseToken(token.replace("Bearer ", ""));
            NftReview review = nftReviewService.reject(nftAssetId, reviewerId, rejectReason, comment);
            return ResponseEntity.ok(ApiResponse.success(review));
        } catch (Exception e) {
            log.error("审核拒绝失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("审核失败：" + e.getMessage()));
        }
    }

    /**
     * 获取NFT审核历史
     */
    @GetMapping("/history/{nftAssetId}")
    public ResponseEntity<ApiResponse<List<NftReview>>> getReviewHistory(@PathVariable Long nftAssetId) {
        try {
            List<NftReview> history = nftReviewService.getReviewHistory(nftAssetId);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            log.error("获取审核历史失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    /**
     * 获取审核详情
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<NftReview>> getReview(@PathVariable Long reviewId) {
        try {
            NftReview review = nftReviewService.getReview(reviewId);
            if (review == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(review));
        } catch (Exception e) {
            log.error("获取审核详情失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }
}