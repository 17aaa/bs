package com.zhm.springboot.userservice.blockchain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IPFS 文件查询条件 DTO
 */
@Data
public class IpfsFileQuery {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 钱包地址
     */
    private String ownerAddress;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件类型列表
     */
    private List<String> fileTypes;

    /**
     * 是否已固定
     */
    private Boolean pinned;

    /**
     * 关联的 NFT ID
     */
    private Long nftAssetId;

    /**
     * 搜索关键词（文件名或描述）
     */
    private String keyword;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String orderBy = "createdAt";

    /**
     * 是否降序
     */
    private Boolean desc = true;
}
