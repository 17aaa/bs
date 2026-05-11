package com.zhm.springboot.userservice.blockchain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IPFS 上传响应 DTO
 */
@Data
@Builder
public class IpfsUploadResponse {

    /**
     * IPFS CID
     */
    private String cid;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * IPFS 网关访问 URL
     */
    private String gatewayUrl;

    /**
     * IPFS 协议 URL (ipfs://)
     */
    private String ipfsUrl;

    /**
     * 是否已固定
     */
    private Boolean pinned;

    /**
     * 上传时间
     */
    private LocalDateTime uploadedAt;

    /**
     * 上传状态
     */
    private String status;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
