package com.zhm.springboot.userservice.admin.controller;

import com.zhm.springboot.userservice.admin.annotation.AdminOnly;
import com.zhm.springboot.userservice.admin.service.AdminDashboardService;
import com.zhm.springboot.userservice.admin.vo.DashboardOverviewVO;
import com.zhm.springboot.userservice.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端仪表盘控制器
 */
@RestController
@RequestMapping("/admin/dashboard")
@AdminOnly
@Slf4j
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * 获取仪表盘概览数据
     */
    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewVO> getOverview() {
        log.info("获取仪表盘概览数据");
        DashboardOverviewVO overview = adminDashboardService.getOverview();
        return ApiResponse.success(overview);
    }
}