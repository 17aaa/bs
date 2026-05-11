package com.zhm.springboot.userservice.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.admin.annotation.SuperAdminOnly;
import com.zhm.springboot.userservice.admin.service.AdminNftService;
import com.zhm.springboot.userservice.admin.vo.AdminNftVO;
import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftVersion;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 NFT 控制器
 */
@RestController
@RequestMapping("/admin/nfts")
@AdminOnly
@Slf4j
public class AdminNftController {

    @Autowired
    private AdminNftService adminNftService;

    /**
     * 获取 NFT 列表
     */
    @GetMapping
    public ApiResponse<Page<AdminNftVO>> getNftList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询 NFT 列表: operator={}, page={}/{}, keyword={}, category={}, status={}",
                currentUserId, pageNum, pageSize, keyword, category, status);

        Page<AdminNftVO> page = new Page<>(pageNum, pageSize);
        Page<AdminNftVO> result = adminNftService.getNftList(page, keyword, category, status);
        return ApiResponse.success(result);
    }

    /**
     * 获取 NFT 详情
     */
    @GetMapping("/{nftId}")
    public ApiResponse<NftAsset> getNftDetail(
            @PathVariable Long nftId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询 NFT 详情: operator={}, nftId={}", currentUserId, nftId);

        NftAsset nft = adminNftService.getNftDetail(nftId);
        if (nft == null) {
            return ApiResponse.error("NFT 不存在");
        }
        return ApiResponse.success(nft);
    }

    /**
     * 获取 NFT 版本历史
     */
    @GetMapping("/{nftId}/versions")
    public ApiResponse<List<NftVersion>> getNftVersions(
            @PathVariable Long nftId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询 NFT 版本历史: operator={}, nftId={}", currentUserId, nftId);

        List<NftVersion> versions = adminNftService.getNftVersions(nftId);
        return ApiResponse.success(versions);
    }

    /**
     * 下架 NFT
     */
    @PostMapping("/{nftId}/delist")
    public ApiResponse<Void> delistNft(
            @PathVariable Long nftId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("下架 NFT: operator={}, nftId={}", currentUserId, nftId);

        boolean success = adminNftService.delistNft(nftId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("下架失败");
    }

    /**
     * 冻结 NFT（超级管理员）
     */
    @PostMapping("/{nftId}/freeze")
    @SuperAdminOnly
    public ApiResponse<Void> freezeNft(
            @PathVariable Long nftId,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("冻结 NFT: operator={}, nftId={}, reason={}", currentUserId, nftId, reason);

        boolean success = adminNftService.freezeNft(nftId, reason);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("冻结失败");
    }

    /**
     * 解冻 NFT（超级管理员）
     */
    @PostMapping("/{nftId}/unfreeze")
    @SuperAdminOnly
    public ApiResponse<Void> unfreezeNft(
            @PathVariable Long nftId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("解冻 NFT: operator={}, nftId={}", currentUserId, nftId);

        boolean success = adminNftService.unfreezeNft(nftId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("解冻失败");
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/category-stats")
    public ApiResponse<List<AdminNftService.CategoryCount>> getCategoryStats(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("获取分类统计: operator={}", currentUserId);

        List<AdminNftService.CategoryCount> stats = adminNftService.getCategoryStats();
        return ApiResponse.success(stats);
    }
}