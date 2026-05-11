package com.zhm.springboot.userservice.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.vo.AdminOrderVO;
import com.zhm.springboot.userservice.admin.vo.OrderStatisticsVO;
import com.zhm.springboot.userservice.market.entity.MarketOrder;

/**
 * 管理端订单服务接口
 */
public interface AdminOrderService {

    /**
     * 获取订单列表
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param status 状态筛选
     * @param orderType 订单类型筛选
     */
    Page<AdminOrderVO> getOrderList(Page<AdminOrderVO> page, String keyword, Integer status, Integer orderType);

    /**
     * 获取订单详情
     * @param orderId 订单 ID
     */
    MarketOrder getOrderDetail(Long orderId);

    /**
     * 获取订单统计
     */
    OrderStatisticsVO getStatistics();
}