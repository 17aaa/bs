package com.zhm.springboot.userservice.nft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * NFT 版本历史实体类
 */
@Data
@TableName("nft_versions")
public class NftVersion {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * NFT 资产 ID
     */
    private Long nftAssetId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 元数据哈希（IPFS CID）
     */
    private String metadataHash;

    /**
     * IPFS URI
     */
    private String ipfsUri;

    /**
     * 变更描述
     */
    private String changeDescription;

    /**
     * 更新者地址
     */
    private String updaterAddress;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 区块号
     */
    private Long blockNumber;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}