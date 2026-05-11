package com.zhm.springboot.userservice.nft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhm.springboot.userservice.audit.entity.AuditLog;
import com.zhm.springboot.userservice.audit.service.AuditLogService;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftReview;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.nft.mapper.NftReviewMapper;
import com.zhm.springboot.userservice.nft.service.NftReviewService;
import com.zhm.springboot.userservice.nft.vo.NftReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * NFT 内容审核服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NftReviewServiceImpl implements NftReviewService {

    private final NftReviewMapper nftReviewMapper;
    private final NftAssetMapper nftAssetMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NftReview submitForReview(Long nftAssetId) {
        NftAsset nftAsset = nftAssetMapper.selectById(nftAssetId);
        if (nftAsset == null) {
            throw new RuntimeException("NFT资产不存在: " + nftAssetId);
        }

        // 更新NFT审核状态为待审核
        nftAsset.setReviewStatus(NftReview.STATUS_PENDING);
        nftAsset.setUpdatedAt(LocalDateTime.now());
        nftAssetMapper.updateById(nftAsset);

        // 创建审核记录
        NftReview review = new NftReview();
        review.setNftAssetId(nftAssetId);
        review.setStatus(NftReview.STATUS_PENDING);
        review.setCreatedAt(LocalDateTime.now());
        nftReviewMapper.insert(review);

        log.info("NFT提交审核: nftAssetId={}, reviewId={}", nftAssetId, review.getId());
        return review;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NftReview approve(Long nftAssetId, Long reviewerId, String comment) {
        NftAsset nftAsset = nftAssetMapper.selectById(nftAssetId);
        if (nftAsset == null) {
            throw new RuntimeException("NFT资产不存在: " + nftAssetId);
        }

        // 更新NFT审核状态为通过
        nftAsset.setReviewStatus(NftReview.STATUS_APPROVED);
        nftAsset.setUpdatedAt(LocalDateTime.now());
        nftAssetMapper.updateById(nftAsset);

        // 更新审核记录
        QueryWrapper<NftReview> wrapper = new QueryWrapper<>();
        wrapper.eq("nft_asset_id", nftAssetId)
               .eq("status", NftReview.STATUS_PENDING)
               .orderByDesc("created_at")
               .last("LIMIT 1");
        NftReview review = nftReviewMapper.selectOne(wrapper);

        if (review != null) {
            review.setStatus(NftReview.STATUS_APPROVED);
            review.setReviewerId(reviewerId);
            review.setComment(comment);
            review.setReviewedAt(LocalDateTime.now());
            nftReviewMapper.updateById(review);
        }

        log.info("NFT审核通过: nftAssetId={}, reviewerId={}", nftAssetId, reviewerId);

        auditLogService.log(reviewerId, AuditLog.ACTION_APPROVE, AuditLog.MODULE_NFT,
                "审核通过NFT: nftAssetId=" + nftAssetId);
        return review;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NftReview reject(Long nftAssetId, Long reviewerId, String rejectReason, String comment) {
        NftAsset nftAsset = nftAssetMapper.selectById(nftAssetId);
        if (nftAsset == null) {
            throw new RuntimeException("NFT资产不存在: " + nftAssetId);
        }

        // 更新NFT审核状态为拒绝
        nftAsset.setReviewStatus(NftReview.STATUS_REJECTED);
        nftAsset.setUpdatedAt(LocalDateTime.now());
        nftAssetMapper.updateById(nftAsset);

        // 更新审核记录
        QueryWrapper<NftReview> wrapper = new QueryWrapper<>();
        wrapper.eq("nft_asset_id", nftAssetId)
               .eq("status", NftReview.STATUS_PENDING)
               .orderByDesc("created_at")
               .last("LIMIT 1");
        NftReview review = nftReviewMapper.selectOne(wrapper);

        if (review != null) {
            review.setStatus(NftReview.STATUS_REJECTED);
            review.setReviewerId(reviewerId);
            review.setRejectReason(rejectReason);
            review.setComment(comment);
            review.setReviewedAt(LocalDateTime.now());
            nftReviewMapper.updateById(review);
        }

        log.info("NFT审核拒绝: nftAssetId={}, reviewerId={}, reason={}", nftAssetId, reviewerId, rejectReason);

        auditLogService.log(reviewerId, AuditLog.ACTION_REJECT, AuditLog.MODULE_NFT,
                "审核拒绝NFT: nftAssetId=" + nftAssetId + ", reason=" + rejectReason);
        return review;
    }

    @Override
    public List<NftReviewVO> getPendingReviews(int page, int size) {
        int offset = (page - 1) * size;

        QueryWrapper<NftReview> wrapper = new QueryWrapper<>();
        wrapper.eq("status", NftReview.STATUS_PENDING)
               .orderByDesc("created_at")
               .last("LIMIT " + size + " OFFSET " + offset);

        List<NftReview> reviews = nftReviewMapper.selectList(wrapper);
        List<NftReviewVO> vos = new ArrayList<>();

        for (NftReview review : reviews) {
            NftAsset nftAsset = nftAssetMapper.selectById(review.getNftAssetId());
            if (nftAsset != null) {
                NftReviewVO vo = new NftReviewVO();
                vo.setId(review.getId());
                vo.setNftAssetId(review.getNftAssetId());
                vo.setNftName(nftAsset.getName());
                vo.setNftImageUrl(nftAsset.getImageUrl());
                vo.setCreatorAddress(nftAsset.getCreatorAddress());
                vo.setStatus(review.getStatus());
                vo.setCreatedAt(review.getCreatedAt());
                vos.add(vo);
            }
        }

        return vos;
    }

    @Override
    public List<NftReview> getReviewHistory(Long nftAssetId) {
        QueryWrapper<NftReview> wrapper = new QueryWrapper<>();
        wrapper.eq("nft_asset_id", nftAssetId)
               .orderByDesc("created_at");
        return nftReviewMapper.selectList(wrapper);
    }

    @Override
    public NftReview getReview(Long reviewId) {
        return nftReviewMapper.selectById(reviewId);
    }

    @Override
    public long getPendingCount() {
        QueryWrapper<NftReview> wrapper = new QueryWrapper<>();
        wrapper.eq("status", NftReview.STATUS_PENDING);
        return nftReviewMapper.selectCount(wrapper);
    }
}