package com.zhm.springboot.userservice.admin.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理端 NFT 列表 VO
 */
@Data
public class AdminNftVO {
    /**
     * NFT ID
     */
    private Long id;

    /**
     * Token ID
     */
    private Long tokenId;

    /**
     * 合约地址
     */
    private String contractAddress;

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
     * 所有者地址
     */
    private String ownerAddress;

    /**
     * 创作者地址
     */
    private String creatorAddress;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 状态：1-正常 2-下架 3-冻结
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 所有者用户名
     */
    private String ownerUsername;

    /**
     * 创作者用户名
     */
    private String creatorUsername;

    /**
     * 是否在售
     */
    private Boolean isOnSale;

    /**
     * 当前售价
     */
    private String currentPrice;
}