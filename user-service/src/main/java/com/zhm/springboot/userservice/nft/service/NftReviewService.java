package com.zhm.springboot.userservice.nft.service;

import com.zhm.springboot.userservice.nft.entity.NftReview;
import com.zhm.springboot.userservice.nft.vo.NftReviewVO;

import java.util.List;

/**
 * NFT 内容审核服务接口
 */
public interface NftReviewService {

    /**
     * 提交NFT进行审核
     */
    NftReview submitForReview(Long nftAssetId);

    /**
     * 审核通过
     */
    NftReview approve(Long nftAssetId, Long reviewerId, String comment);

    /**
     * 审核拒绝
     */
    NftReview reject(Long nftAssetId, Long reviewerId, String rejectReason, String comment);

    /**
     * 获取待审核列表
     */
    List<NftReviewVO> getPendingReviews(int page, int size);

    /**
     * 获取NFT的审核记录
     */
    List<NftReview> getReviewHistory(Long nftAssetId);

    /**
     * 获取审核详情
     */
    NftReview getReview(Long reviewId);

    /**
     * 获取待审核数量
     */
    long getPendingCount();
}