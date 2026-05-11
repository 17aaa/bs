package com.zhm.springboot.userservice.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.service.AdminNftService;
import com.zhm.springboot.userservice.admin.vo.AdminNftVO;
import com.zhm.springboot.userservice.entity.User;
import com.zhm.springboot.userservice.mapper.UserMapper;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftVersion;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.nft.mapper.NftVersionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端 NFT 服务实现
 */
@Service
@Slf4j
public class AdminNftServiceImpl implements AdminNftService {

    @Autowired
    private NftAssetMapper nftAssetMapper;

    @Autowired
    private NftVersionMapper nftVersionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<AdminNftVO> getNftList(Page<AdminNftVO> page, String keyword, String category, Integer status) {
        log.info("获取 NFT 列表: keyword={}, category={}, status={}", keyword, category, status);

        Page<NftAsset> nftPage = new Page<>(page.getCurrent(), page.getSize());
        QueryWrapper<NftAsset> wrapper = new QueryWrapper<>();

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("name", keyword)
                    .or().like("description", keyword)
                    .or().like("creator_address", keyword)
                    .or().like("owner_address", keyword));
        }

        // 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq("category", category);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq("status", status);
        }

        // 排序
        wrapper.orderByDesc("created_at");

        nftAssetMapper.selectPage(nftPage, wrapper);

        // 收集所有地址
        List<String> addresses = new ArrayList<>();
        for (NftAsset nft : nftPage.getRecords()) {
            if (nft.getOwnerAddress() != null) addresses.add(nft.getOwnerAddress());
            if (nft.getCreatorAddress() != null) addresses.add(nft.getCreatorAddress());
        }

        // 查询用户映射
        Map<String, String> addressToUsername = new HashMap<>();
        if (!addresses.isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            // 这里需要通过 wallet 表关联，简化处理
        }

        // 转换为 VO
        List<AdminNftVO> voList = nftPage.getRecords().stream().map(nft -> {
            AdminNftVO vo = new AdminNftVO();
            BeanUtils.copyProperties(nft, vo);
            vo.setOwnerUsername(addressToUsername.getOrDefault(nft.getOwnerAddress(), "未知"));
            vo.setCreatorUsername(addressToUsername.getOrDefault(nft.getCreatorAddress(), "未知"));
            vo.setIsOnSale(false); // 简化处理
            return vo;
        }).collect(Collectors.toList());

        page.setRecords(voList);
        page.setTotal(nftPage.getTotal());

        return page;
    }

    @Override
    public NftAsset getNftDetail(Long nftId) {
        log.info("获取 NFT 详情: nftId={}", nftId);
        return nftAssetMapper.selectById(nftId);
    }

    @Override
    public List<NftVersion> getNftVersions(Long nftId) {
        log.info("获取 NFT 版本历史: nftId={}", nftId);
        QueryWrapper<NftVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("nft_asset_id", nftId).orderByDesc("version");
        return nftVersionMapper.selectList(wrapper);
    }

    @Override
    public boolean delistNft(Long nftId) {
        log.info("下架 NFT: nftId={}", nftId);

        NftAsset nft = nftAssetMapper.selectById(nftId);
        if (nft == null) {
            return false;
        }

        nft.setStatus(2); // 下架状态
        return nftAssetMapper.updateById(nft) > 0;
    }

    @Override
    public boolean freezeNft(Long nftId, String reason) {
        log.info("冻结 NFT: nftId={}, reason={}", nftId, reason);

        NftAsset nft = nftAssetMapper.selectById(nftId);
        if (nft == null) {
            return false;
        }

        nft.setStatus(3); // 冻结状态
        return nftAssetMapper.updateById(nft) > 0;
    }

    @Override
    public boolean unfreezeNft(Long nftId) {
        log.info("解冻 NFT: nftId={}", nftId);

        NftAsset nft = nftAssetMapper.selectById(nftId);
        if (nft == null) {
            return false;
        }

        nft.setStatus(1); // 正常状态
        return nftAssetMapper.updateById(nft) > 0;
    }

    @Override
    public List<CategoryCount> getCategoryStats() {
        log.info("获取分类统计");

        List<NftAsset> allNfts = nftAssetMapper.selectList(null);
        Map<String, Long> categoryCountMap = new HashMap<>();

        for (NftAsset nft : allNfts) {
            String category = nft.getCategory() != null ? nft.getCategory() : "未分类";
            categoryCountMap.merge(category, 1L, Long::sum);
        }

        return categoryCountMap.entrySet().stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .collect(Collectors.toList());
    }
}