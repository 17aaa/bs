package com.zhm.springboot.userservice.nft.service;

import com.zhm.springboot.userservice.nft.entity.MintTask;

/**
 * 铸造任务服务接口
 */
public interface MintTaskService {

    /**
     * 创建铸造任务
     */
    MintTask createTask(Long userId, String creatorAddress, String name, String description,
                        String category, String imageUrl, Integer royaltyFee);

    /**
     * 获取任务详情
     */
    MintTask getTask(String taskId);

    /**
     * 更新任务进度
     */
    void updateProgress(String taskId, int step, String status, int progressPercent, String errorMessage);

    /**
     * 完成任务
     */
    void completeTask(String taskId, Long nftAssetId, String txHash);

    /**
     * 执行铸造流程（异步）
     */
    void executeMintTask(String taskId);

    /**
     * 处理上传完成
     */
    void handleUploadComplete(String taskId, String imageUrl);

    /**
     * 处理元数据生成完成
     */
    void handleMetadataComplete(String taskId, String metadataUrl);

    /**
     * 回滚铸造任务（清理部分创建的数据）
     */
    void rollbackTask(String taskId);

    /**
     * 重试铸造任务
     */
    MintTask retryTask(String taskId);

    /**
     * 取消铸造任务
     */
    void cancelTask(String taskId);
}