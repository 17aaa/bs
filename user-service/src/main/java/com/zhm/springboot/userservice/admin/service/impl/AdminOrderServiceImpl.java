package com.zhm.springboot.userservice.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.service.AdminOrderService;
import com.zhm.springboot.userservice.admin.vo.AdminOrderVO;
import com.zhm.springboot.userservice.admin.vo.OrderStatisticsVO;
import com.zhm.springboot.userservice.market.entity.MarketOrder;
import com.zhm.springboot.userservice.market.mapper.MarketOrderMapper;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端订单服务实现
 */
@Service
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

    @Autowired
    private MarketOrderMapper marketOrderMapper;

    @Autowired
    private NftAssetMapper nftAssetMapper;

    @Override
    public Page<AdminOrderVO> getOrderList(Page<AdminOrderVO> page, String keyword, Integer status, Integer orderType) {
        log.info("获取订单列表: keyword={}, status={}, orderType={}", keyword, status, orderType);

        Page<MarketOrder> orderPage = new Page<>(page.getCurrent(), page.getSize());
        QueryWrapper<MarketOrder> wrapper = new QueryWrapper<>();

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("order_id", keyword)
                    .or().like("seller_address", keyword)
                    .or().like("buyer_address", keyword));
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq("status", status);
        }

        // 订单类型筛选
        if (orderType != null) {
            wrapper.eq("order_type", orderType);
        }

        // 排序
        wrapper.orderByDesc("created_at");

        marketOrderMapper.selectPage(orderPage, wrapper);

        // 收集 NFT ID
        List<Long> nftIds = orderPage.getRecords().stream()
                .filter(o -> o.getNftAssetId() != null)
                .map(MarketOrder::getNftAssetId)
                .distinct()
                .collect(Collectors.toList());

        // 查询 NFT 信息
        Map<Long, NftAsset> nftMap = new HashMap<>();
        if (!nftIds.isEmpty()) {
            List<NftAsset> nfts = nftAssetMapper.selectBatchIds(nftIds);
            for (NftAsset nft : nfts) {
                nftMap.put(nft.getId(), nft);
            }
        }

        // 转换为 VO
        List<AdminOrderVO> voList = orderPage.getRecords().stream().map(order -> {
            AdminOrderVO vo = new AdminOrderVO();
            BeanUtils.copyProperties(order, vo);

            // 填充 NFT 信息
            if (order.getNftAssetId() != null) {
                NftAsset nft = nftMap.get(order.getNftAssetId());
                if (nft != null) {
                    vo.setNftName(nft.getName());
                    vo.setNftImageUrl(nft.getImageUrl());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        page.setRecords(voList);
        page.setTotal(orderPage.getTotal());

        return page;
    }

    @Override
    public MarketOrder getOrderDetail(Long orderId) {
        log.info("获取订单详情: orderId={}", orderId);
        return marketOrderMapper.selectById(orderId);
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        log.info("获取订单统计");

        OrderStatisticsVO stats = new OrderStatisticsVO();

        // 总订单数
        stats.setTotalOrders(marketOrderMapper.selectCount(null));

        // 各状态订单数
        QueryWrapper<MarketOrder> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("status", 1);
        stats.setActiveOrders(marketOrderMapper.selectCount(activeWrapper));

        QueryWrapper<MarketOrder> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("status", 2);
        Long completedCount = marketOrderMapper.selectCount(completedWrapper);
        stats.setCompletedOrders(completedCount);

        QueryWrapper<MarketOrder> cancelledWrapper = new QueryWrapper<>();
        cancelledWrapper.eq("status", 3);
        stats.setCancelledOrders(marketOrderMapper.selectCount(cancelledWrapper));

        // 交易额统计
        List<MarketOrder> completedOrders = marketOrderMapper.selectList(completedWrapper);
        BigInteger totalVolume = BigInteger.ZERO;
        BigInteger highestPrice = BigInteger.ZERO;

        for (MarketOrder order : completedOrders) {
            if (order.getFinalPrice() != null) {
                totalVolume = totalVolume.add(order.getFinalPrice());
                if (order.getFinalPrice().compareTo(highestPrice) > 0) {
                    highestPrice = order.getFinalPrice();
                }
            }
        }

        stats.setTotalVolume(totalVolume);
        stats.setHighestPrice(highestPrice);

        if (completedCount > 0 && !totalVolume.equals(BigInteger.ZERO)) {
            BigDecimal avgPrice = new BigDecimal(totalVolume).divide(BigDecimal.valueOf(completedCount), 0, RoundingMode.DOWN);
            stats.setAveragePrice(avgPrice.toBigInteger());
        } else {
            stats.setAveragePrice(BigInteger.ZERO);
        }

        // 分类统计 - 简化处理
        stats.setCategoryStats(new ArrayList<>());

        return stats;
    }
}