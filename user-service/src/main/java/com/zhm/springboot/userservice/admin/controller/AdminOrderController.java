package com.zhm.springboot.userservice.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.admin.service.AdminOrderService;
import com.zhm.springboot.userservice.admin.vo.AdminOrderVO;
import com.zhm.springboot.userservice.admin.vo.OrderStatisticsVO;
import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.market.entity.MarketOrder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端订单控制器
 */
@RestController
@RequestMapping("/admin/orders")
@AdminOnly
@Slf4j
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    /**
     * 获取订单列表
     */
    @GetMapping
    public ApiResponse<Page<AdminOrderVO>> getOrderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer orderType,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询订单列表: operator={}, page={}/{}, keyword={}, status={}, orderType={}",
                currentUserId, pageNum, pageSize, keyword, status, orderType);

        Page<AdminOrderVO> page = new Page<>(pageNum, pageSize);
        Page<AdminOrderVO> result = adminOrderService.getOrderList(page, keyword, status, orderType);
        return ApiResponse.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ApiResponse<MarketOrder> getOrderDetail(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("查询订单详情: operator={}, orderId={}", currentUserId, orderId);

        MarketOrder order = adminOrderService.getOrderDetail(orderId);
        if (order == null) {
            return ApiResponse.error("订单不存在");
        }
        return ApiResponse.success(order);
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/statistics")
    public ApiResponse<OrderStatisticsVO> getStatistics(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        log.info("获取订单统计: operator={}", currentUserId);

        OrderStatisticsVO stats = adminOrderService.getStatistics();
        return ApiResponse.success(stats);
    }
}