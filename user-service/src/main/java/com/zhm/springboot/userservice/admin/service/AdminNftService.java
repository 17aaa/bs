package com.zhm.springboot.userservice.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.vo.AdminNftVO;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftVersion;

import java.util.List;

/**
 * 管理端 NFT 服务接口
 */
public interface AdminNftService {

    /**
     * 获取 NFT 列表
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param category 分类筛选
     * @param status 状态筛选
     */
    Page<AdminNftVO> getNftList(Page<AdminNftVO> page, String keyword, String category, Integer status);

    /**
     * 获取 NFT 详情
     * @param nftId NFT ID
     */
    NftAsset getNftDetail(Long nftId);

    /**
     * 获取 NFT 版本历史
     * @param nftId NFT ID
     */
    List<NftVersion> getNftVersions(Long nftId);

    /**
     * 下架 NFT
     * @param nftId NFT ID
     */
    boolean delistNft(Long nftId);

    /**
     * 冻结 NFT
     * @param nftId NFT ID
     * @param reason 冻结原因
     */
    boolean freezeNft(Long nftId, String reason);

    /**
     * 解冻 NFT
     * @param nftId NFT ID
     */
    boolean unfreezeNft(Long nftId);

    /**
     * 获取分类统计
     */
    List<CategoryCount> getCategoryStats();

    /**
     * 分类统计
     */
    record CategoryCount(String category, Long count) {}
}