package com.zhm.springboot.userservice.nft.vo;

import lombok.Data;

/**
 * 铸造任务进度 VO
 */
@Data
public class MintTaskVO {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 作品名称
     */
    private String name;

    /**
     * 当前步骤：1-上传 2-元数据生成 3-链上铸造 4-完成
     */
    private Integer currentStep;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤状态：pending/processing/completed/failed
     */
    private String stepStatus;

    /**
     * 进度百分比 0-100
     */
    private Integer progressPercent;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * NFT资产ID（铸造成功后）
     */
    private Long nftAssetId;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 是否完成
     */
    private Boolean completed;

    /**
     * 是否失败
     */
    private Boolean failed;

    /**
     * 回滚状态
     */
    private String rollbackStatus;

    /**
     * 回滚错误信息
     */
    private String rollbackError;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 是否可以重试
     */
    private Boolean canRetry;

    /**
     * 步骤列表
     */
    private java.util.List<StepInfo> steps;

    @Data
    public static class StepInfo {
        private Integer step;
        private String name;
        private String status; // pending/processing/completed/failed
    }
}