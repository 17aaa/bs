package com.zhm.springboot.userservice.admin.service;

import com.zhm.springboot.userservice.admin.vo.DashboardOverviewVO;

/**
 * 管理端仪表盘服务接口
 */
public interface AdminDashboardService {

    /**
     * 获取仪表盘概览数据
     */
    DashboardOverviewVO getOverview();
}