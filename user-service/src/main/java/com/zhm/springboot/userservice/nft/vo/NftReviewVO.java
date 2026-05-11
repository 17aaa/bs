package com.zhm.springboot.userservice.nft.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * NFT 审核记录 VO
 */
@Data
public class NftReviewVO {

    private Long id;

    private Long nftAssetId;

    private String nftName;

    private String nftImageUrl;

    private String creatorAddress;

    private Long reviewerId;

    private String reviewerName;

    private String status;

    private String comment;

    private String rejectReason;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
}