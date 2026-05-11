package com.zhm.springboot.userservice.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.admin.service.AdminFanTokenService;
import com.zhm.springboot.userservice.admin.vo.AdminFanTokenVO;
import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.fantoken.entity.FanToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端粉丝代币控制器
 */
@RestController
@RequestMapping("/admin/fan-tokens")
@AdminOnly
@Slf4j
public class AdminFanTokenController {

    @Autowired
    private AdminFanTokenService adminFanTokenService;

    /**
     * 获取粉丝代币列表
     */
    @GetMapping
    public ApiResponse<Page<AdminFanTokenVO>> getFanTokenList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询粉丝代币列表: operator={}, page={}/{}, keyword={}, status={}",
                currentUserId, pageNum, pageSize, keyword, status);

        Page<AdminFanTokenVO> page = new Page<>(pageNum, pageSize);
        Page<AdminFanTokenVO> result = adminFanTokenService.getFanTokenList(page, keyword, status);
        return ApiResponse.success(result);
    }

    /**
     * 获取粉丝代币详情
     */
    @GetMapping("/{tokenId}")
    public ApiResponse<FanToken> getFanTokenDetail(
            @PathVariable Long tokenId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询粉丝代币详情: operator={}, tokenId={}", currentUserId, tokenId);

        FanToken token = adminFanTokenService.getFanTokenDetail(tokenId);
        if (token == null) {
            return ApiResponse.error("代币不存在");
        }
        return ApiResponse.success(token);
    }

    /**
     * 激活公募
     */
    @PostMapping("/{tokenId}/activate")
    public ApiResponse<Void> activatePublicSale(
            @PathVariable Long tokenId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("激活公募: operator={}, tokenId={}", currentUserId, tokenId);

        boolean success = adminFanTokenService.activatePublicSale(tokenId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("激活失败");
    }

    /**
     * 暂停公募
     */
    @PostMapping("/{tokenId}/pause")
    public ApiResponse<Void> pausePublicSale(
            @PathVariable Long tokenId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("暂停公募: operator={}, tokenId={}", currentUserId, tokenId);

        boolean success = adminFanTokenService.pausePublicSale(tokenId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("暂停失败");
    }

    /**
     * 结束公募
     */
    @PostMapping("/{tokenId}/end")
    public ApiResponse<Void> endPublicSale(
            @PathVariable Long tokenId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("结束公募: operator={}, tokenId={}", currentUserId, tokenId);

        boolean success = adminFanTokenService.endPublicSale(tokenId);
        if (success) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("结束失败");
    }
}