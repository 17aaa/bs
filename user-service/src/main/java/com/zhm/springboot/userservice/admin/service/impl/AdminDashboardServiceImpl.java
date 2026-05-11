package com.zhm.springboot.userservice.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhm.springboot.userservice.admin.service.AdminDashboardService;
import com.zhm.springboot.userservice.admin.vo.DashboardOverviewVO;
import com.zhm.springboot.userservice.entity.User;
import com.zhm.springboot.userservice.fantoken.entity.FanToken;
import com.zhm.springboot.userservice.fantoken.mapper.FanTokenMapper;
import com.zhm.springboot.userservice.market.entity.MarketOrder;
import com.zhm.springboot.userservice.market.mapper.MarketOrderMapper;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端仪表盘服务实现
 */
@Service
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NftAssetMapper nftAssetMapper;

    @Autowired
    private MarketOrderMapper marketOrderMapper;

    @Autowired
    private FanTokenMapper fanTokenMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        log.info("获取仪表盘概览数据");

        DashboardOverviewVO overview = new DashboardOverviewVO();

        // 用户统计
        overview.setTotalUsers(userMapper.selectCount(null));
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        QueryWrapper<User> todayUserWrapper = new QueryWrapper<>();
        todayUserWrapper.ge("gmt_create", todayStart);
        overview.setTodayNewUsers(userMapper.selectCount(todayUserWrapper));

        // NFT 统计
        overview.setTotalNfts(nftAssetMapper.selectCount(null));
        QueryWrapper<NftAsset> todayNftWrapper = new QueryWrapper<>();
        todayNftWrapper.ge("created_at", todayStart);
        overview.setTodayNewNfts(nftAssetMapper.selectCount(todayNftWrapper));

        // 交易统计
        QueryWrapper<MarketOrder> completedOrderWrapper = new QueryWrapper<>();
        completedOrderWrapper.eq("status", 2); // 已售
        List<MarketOrder> completedOrders = marketOrderMapper.selectList(completedOrderWrapper);

        BigInteger totalVolume = BigInteger.ZERO;
        for (MarketOrder order : completedOrders) {
            if (order.getFinalPrice() != null) {
                totalVolume = totalVolume.add(order.getFinalPrice());
            }
        }
        overview.setTotalTradeVolume(totalVolume);
        overview.setTotalTrades((long) completedOrders.size());

        // 今日交易
        QueryWrapper<MarketOrder> todayOrderWrapper = new QueryWrapper<>();
        todayOrderWrapper.eq("status", 2);
        todayOrderWrapper.ge("updated_at", todayStart);
        List<MarketOrder> todayOrders = marketOrderMapper.selectList(todayOrderWrapper);

        BigInteger todayVolume = BigInteger.ZERO;
        for (MarketOrder order : todayOrders) {
            if (order.getFinalPrice() != null) {
                todayVolume = todayVolume.add(order.getFinalPrice());
            }
        }
        overview.setTodayTradeVolume(todayVolume);
        overview.setTodayTrades((long) todayOrders.size());

        // 粉丝代币统计
        overview.setTotalFanTokens(fanTokenMapper.selectCount(null));

        // 最近活动
        List<DashboardOverviewVO.RecentActivity> activities = new ArrayList<>();

        // 最近注册用户
        QueryWrapper<User> recentUserWrapper = new QueryWrapper<>();
        recentUserWrapper.orderByDesc("gmt_create").last("LIMIT 3");
        List<User> recentUsers = userMapper.selectList(recentUserWrapper);
        for (User user : recentUsers) {
            DashboardOverviewVO.RecentActivity activity = new DashboardOverviewVO.RecentActivity();
            activity.setType("register");
            activity.setDescription("新用户注册");
            activity.setUsername(user.getUsername());
            activity.setTime(formatTime(user.getGmtCreate()));
            activities.add(activity);
        }

        // 最近铸造 NFT
        QueryWrapper<NftAsset> recentNftWrapper = new QueryWrapper<>();
        recentNftWrapper.orderByDesc("created_at").last("LIMIT 3");
        List<NftAsset> recentNfts = nftAssetMapper.selectList(recentNftWrapper);
        for (NftAsset nft : recentNfts) {
            DashboardOverviewVO.RecentActivity activity = new DashboardOverviewVO.RecentActivity();
            activity.setType("mint");
            activity.setDescription("铸造新 NFT: " + nft.getName());
            activity.setTime(formatTime(nft.getCreatedAt()));
            activities.add(activity);
        }

        // 最近交易
        QueryWrapper<MarketOrder> recentOrderWrapper = new QueryWrapper<>();
        recentOrderWrapper.eq("status", 2).orderByDesc("updated_at").last("LIMIT 3");
        List<MarketOrder> recentOrders = marketOrderMapper.selectList(recentOrderWrapper);
        for (MarketOrder order : recentOrders) {
            DashboardOverviewVO.RecentActivity activity = new DashboardOverviewVO.RecentActivity();
            activity.setType("trade");
            activity.setDescription("NFT 交易完成");
            activity.setTime(formatTime(order.getUpdatedAt()));
            activities.add(activity);
        }

        overview.setRecentActivities(activities);

        return overview;
    }

    private String formatTime(java.time.LocalDateTime time) {
        if (time == null) return "";
        long minutes = java.time.Duration.between(time, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + " 分钟前";
        long hours = minutes / 60;
        if (hours < 24) return hours + " 小时前";
        long days = hours / 24;
        return days + " 天前";
    }

    private String formatTime(java.sql.Timestamp time) {
        if (time == null) return "";
        return formatTime(time.toLocalDateTime());
    }
}