package com.zhm.springboot.userservice.nft.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * NFT 铸造任务实体类
 */
@Data
@TableName("mint_tasks")
public class MintTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 图片URL
     */
    private String imageUrl;

    /**
     * 元数据URL
     */
    private String metadataUrl;

    /**
     * 版税比例（万分比）
     */
    private Integer royaltyFee;

    /**
     * 当前步骤：1-上传 2-元数据生成 3-链上铸造 4-完成
     */
    private Integer currentStep;

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
     * 铸造成功后的NFT资产ID
     */
    private Long nftAssetId;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 回滚状态：none/pending/completed/failed
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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 步骤常量
    public static final int STEP_UPLOAD = 1;
    public static final int STEP_METADATA = 2;
    public static final int STEP_MINTING = 3;
    public static final int STEP_COMPLETED = 4;

    // 状态常量
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";

    // 回滚状态常量
    public static final String ROLLBACK_NONE = "none";
    public static final String ROLLBACK_PENDING = "pending";
    public static final String ROLLBACK_COMPLETED = "completed";
    public static final String ROLLBACK_FAILED = "failed";

    // 最大重试次数
    public static final int MAX_RETRY_COUNT = 3;

    /**
     * 获取步骤名称
     */
    public String getStepName() {
        switch (currentStep) {
            case STEP_UPLOAD: return "文件上传";
            case STEP_METADATA: return "元数据生成";
            case STEP_MINTING: return "链上铸造";
            case STEP_COMPLETED: return "铸造完成";
            default: return "未知步骤";
        }
    }
}