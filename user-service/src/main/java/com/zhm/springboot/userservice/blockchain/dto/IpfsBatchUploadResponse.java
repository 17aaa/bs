package com.zhm.springboot.userservice.blockchain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * IPFS 批量上传响应 DTO
 */
@Data
@Builder
public class IpfsBatchUploadResponse {

    /**
     * 成功上传的文件列表
     */
    private List<IpfsUploadResponse> successful;

    /**
     * 上传失败的文件列表
     */
    private List<IpfsUploadResponse> failed;

    /**
     * 总文件数
     */
    private Integer totalCount;

    /**
     * 成功数
     */
    private Integer successCount;

    /**
     * 失败数
     */
    private Integer failedCount;

    /**
     * 总大小（字节）
     */
    private Long totalSize;
}
