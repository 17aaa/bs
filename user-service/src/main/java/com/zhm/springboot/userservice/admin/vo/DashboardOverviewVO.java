package com.zhm.springboot.userservice.admin.vo;

import lombok.Data;
import java.math.BigInteger;
import java.util.List;

/**
 * 仪表盘概览 VO
 */
@Data
public class DashboardOverviewVO {
    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 今日新增用户
     */
    private Long todayNewUsers;

    /**
     * 总 NFT 数量
     */
    private Long totalNfts;

    /**
     * 今日新增 NFT
     */
    private Long todayNewNfts;

    /**
     * 总交易额（Wei）
     */
    private BigInteger totalTradeVolume;

    /**
     * 今日交易额（Wei）
     */
    private BigInteger todayTradeVolume;

    /**
     * 总交易次数
     */
    private Long totalTrades;

    /**
     * 今日交易次数
     */
    private Long todayTrades;

    /**
     * 粉丝代币数量
     */
    private Long totalFanTokens;

    /**
     * 最近活动列表
     */
    private List<RecentActivity> recentActivities;

    /**
     * 最近活动
     */
    @Data
    public static class RecentActivity {
        /**
         * 活动类型：register, mint, trade, etc.
         */
        private String type;

        /**
         * 活动描述
         */
        private String description;

        /**
         * 活动时间
         */
        private String time;

        /**
         * 相关用户
         */
        private String username;
    }
}