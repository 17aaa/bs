package com.zhm.springboot.userservice.nft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * NFT 资产实体类
 */
@Data
@TableName("nft_assets")
public class NftAsset {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Token ID（链上）
     */
    private Long tokenId;

    /**
     * 合约地址
     */
    private String contractAddress;

    /**
     * 所有者地址
     */
    private String ownerAddress;

    /**
     * 创作者地址
     */
    private String creatorAddress;

    /**
     * 作品名称
     */
    private String name;

    /**
     * 作品描述
     */
    private String description;

    /**
     * 分类
     */
    private String category;

    /**
     * 图片 URL
     */
    private String imageUrl;

    /**
     * 当前元数据哈希（IPFS CID）
     */
    private String currentMetadataHash;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 版税比例（万分比，500 = 5%）
     */
    private Integer royaltyFee;

    /**
     * 版税接收地址
     */
    private String royaltyRecipient;

    /**
     * 状态：1-正常 2-下架 3-冻结
     */
    private Integer status;

    /**
     * 审核状态：pending/approved/rejected
     */
    private String reviewStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}