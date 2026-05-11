package com.zhm.springboot.userservice.blockchain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * IPFS 统计信息 DTO
 */
@Data
@Builder
public class IpfsStatistics {

    /**
     * 总文件数
     */
    private Long totalFiles;

    /**
     * 总存储大小（字节）
     */
    private Long totalSize;

    /**
     * 总访问次数
     */
    private Long totalAccessCount;

    /**
     * 已固定文件数
     */
    private Long pinnedFiles;

    /**
     * 未固定文件数
     */
    private Long unpinnedFiles;

    /**
     * 按类型统计
     */
    private Map<String, TypeStatistics> byType;

    /**
     * 平均文件大小
     */
    private Long averageFileSize;

    /**
     * 最大文件大小
     */
    private Long maxFileSize;

    /**
     * 今日上传数
     */
    private Long todayUploads;

    /**
     * 类型统计
     */
    @Data
    @Builder
    public static class TypeStatistics {
        private Long count;
        private Long totalSize;
        private Double percentage;
    }
}
