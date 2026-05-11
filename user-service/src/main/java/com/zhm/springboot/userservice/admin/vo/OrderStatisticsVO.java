package com.zhm.springboot.userservice.admin.vo;

import lombok.Data;
import java.math.BigInteger;
import java.util.List;

/**
 * 订单统计 VO
 */
@Data
public class OrderStatisticsVO {
    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 活跃订单数
     */
    private Long activeOrders;

    /**
     * 已完成订单数
     */
    private Long completedOrders;

    /**
     * 已取消订单数
     */
    private Long cancelledOrders;

    /**
     * 总交易额（Wei）
     */
    private BigInteger totalVolume;

    /**
     * 平均交易价格（Wei）
     */
    private BigInteger averagePrice;

    /**
     * 最高交易价格（Wei）
     */
    private BigInteger highestPrice;

    /**
     * 按分类统计
     */
    private List<CategoryStats> categoryStats;

    /**
     * 分类统计
     */
    @Data
    public static class CategoryStats {
        /**
         * 分类名称
         */
        private String category;

        /**
         * 交易数量
         */
        private Long count;

        /**
         * 交易额
         */
        private BigInteger volume;
    }
}