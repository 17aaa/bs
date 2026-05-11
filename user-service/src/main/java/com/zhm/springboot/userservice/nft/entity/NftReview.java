package com.zhm.springboot.userservice.nft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * NFT 内容审核记录实体
 */
@Data
@TableName("nft_reviews")
public class NftReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * NFT资产ID
     */
    private Long nftAssetId;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核状态：pending/approved/rejected
     */
    private String status;

    /**
     * 审核意见
     */
    private String comment;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // 审核状态常量
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    // 拒绝原因类型
    public static final String REASON_INAPPROPRIATE = "inappropriate";
    public static final String REASON_COPYRIGHT = "copyright";
    public static final String REASON_SPAM = "spam";
    public static final String REASON_OTHER = "other";
}