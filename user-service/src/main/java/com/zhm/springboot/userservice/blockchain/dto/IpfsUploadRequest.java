package com.zhm.springboot.userservice.blockchain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IPFS 上传请求 DTO
 */
@Data
public class IpfsUploadRequest {

    /**
     * 文件描述
     */
    private String description;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 是否立即固定文件
     */
    private Boolean pin = true;

    /**
     * 关联的 NFT ID
     */
    private Long nftAssetId;

    /**
     * 自定义元数据
     */
    private Map<String, Object> metadata;
}
