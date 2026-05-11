package com.zhm.springboot.userservice.blockchain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IPFS 文件上传记录实体
 * 用于追踪和管理上传到 IPFS 的文件
 */
@Data
@TableName("ipfs_file_record")
public class IpfsFileRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * IPFS Content Identifier (CID)
     */
    private String cid;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件类型 (image, json, video, audio, other)
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * 文件是否已固定（防止被垃圾回收）
     */
    private Boolean pinned;

    /**
     * 固定时间
     */
    private LocalDateTime pinnedAt;

    /**
     * 上传者用户ID
     */
    private Long userId;

    /**
     * 上传者钱包地址
     */
    private String ownerAddress;

    /**
     * 关联的 NFT ID（如果有）
     */
    private Long nftAssetId;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 标签，逗号分隔
     */
    private String tags;

    /**
     * 访问次数
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer accessCount;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;

    /**
     * 文件状态: active(正常), deleted(已删除), expired(已过期)
     */
    @TableField(fill = FieldFill.INSERT)
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
