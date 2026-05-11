package com.zhm.springboot.userservice.nft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhm.springboot.userservice.nft.entity.MintTask;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftReview;
import com.zhm.springboot.userservice.nft.entity.NftVersion;
import com.zhm.springboot.userservice.nft.mapper.MintTaskMapper;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.nft.mapper.NftVersionMapper;
import com.zhm.springboot.userservice.nft.service.MintTaskService;
import com.zhm.springboot.userservice.nft.service.NftReviewService;
import com.zhm.springboot.userservice.blockchain.config.ContractProperties;
import com.zhm.springboot.userservice.blockchain.config.IpfsProperties;
import com.zhm.springboot.userservice.blockchain.service.IpfsService;
import com.zhm.springboot.userservice.blockchain.service.IpfsServiceEnhanced;
import com.zhm.springboot.userservice.nft.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 铸造任务服务实现
 * 铸造流程：上传文件 → 生成元数据并上传 IPFS → 链上铸造 → 完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MintTaskServiceImpl implements MintTaskService {

    private final MintTaskMapper mintTaskMapper;
    private final NftAssetMapper nftAssetMapper;
    private final NftVersionMapper nftVersionMapper;
    private final ContractProperties contractProperties;
    private final NftReviewService nftReviewService;
    private final IpfsService ipfsService;
    private final IpfsServiceEnhanced ipfsServiceEnhanced;
    private final IpfsProperties ipfsProperties;
    private final MinioService minioService;

    @Override
    public MintTask createTask(Long userId, String creatorAddress, String name, String description,
                                String category, String imageUrl, Integer royaltyFee) {
        MintTask task = new MintTask();
        task.setTaskId("MT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8));
        task.setUserId(userId);
        task.setCreatorAddress(creatorAddress);
        task.setName(name);
        task.setDescription(description);
        task.setCategory(category);
        task.setImageUrl(imageUrl);
        task.setRoyaltyFee(royaltyFee != null ? royaltyFee : 500);
        task.setCurrentStep(MintTask.STEP_UPLOAD);
        task.setStepStatus(MintTask.STATUS_PENDING);
        task.setProgressPercent(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        mintTaskMapper.insert(task);
        log.info("创建铸造任务: taskId={}, userId={}, name={}", task.getTaskId(), userId, name);

        return task;
    }

    @Override
    public MintTask getTask(String taskId) {
        QueryWrapper<MintTask> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        return mintTaskMapper.selectOne(wrapper);
    }

    @Override
    public void updateProgress(String taskId, int step, String status, int progressPercent, String errorMessage) {
        MintTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: taskId={}", taskId);
            return;
        }

        task.setCurrentStep(step);
        task.setStepStatus(status);
        task.setProgressPercent(progressPercent);
        if (errorMessage != null) {
            task.setErrorMessage(errorMessage);
        }
        task.setUpdatedAt(LocalDateTime.now());

        mintTaskMapper.updateById(task);
        log.info("更新任务进度: taskId={}, step={}, status={}, progress={}%",
                taskId, step, status, progressPercent);
    }

    @Override
    public void completeTask(String taskId, Long nftAssetId, String txHash) {
        MintTask task = getTask(taskId);
        if (task == null) {
            log.warn("任务不存在: taskId={}", taskId);
            return;
        }

        task.setCurrentStep(MintTask.STEP_COMPLETED);
        task.setStepStatus(MintTask.STATUS_COMPLETED);
        task.setProgressPercent(100);
        task.setNftAssetId(nftAssetId);
        task.setTxHash(txHash);
        task.setUpdatedAt(LocalDateTime.now());

        mintTaskMapper.updateById(task);
        log.info("任务完成: taskId={}, nftAssetId={}", taskId, nftAssetId);
    }

    @Override
    public void handleUploadComplete(String taskId, String imageUrl) {
        MintTask task = getTask(taskId);
        if (task == null) return;

        task.setImageUrl(imageUrl);
        task.setCurrentStep(MintTask.STEP_METADATA);
        task.setStepStatus(MintTask.STATUS_PENDING);
        task.setProgressPercent(25);
        task.setUpdatedAt(LocalDateTime.now());
        mintTaskMapper.updateById(task);
        log.info("上传完成: taskId={}, imageUrl={}", taskId, imageUrl);
    }

    @Override
    public void handleMetadataComplete(String taskId, String metadataUrl) {
        MintTask task = getTask(taskId);
        if (task == null) return;

        task.setMetadataUrl(metadataUrl);
        task.setCurrentStep(MintTask.STEP_MINTING);
        task.setStepStatus(MintTask.STATUS_PENDING);
        task.setProgressPercent(50);
        task.setUpdatedAt(LocalDateTime.now());
        mintTaskMapper.updateById(task);
        log.info("元数据生成完成: taskId={}, metadataUrl={}", taskId, metadataUrl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackTask(String taskId) {
        MintTask task = getTask(taskId);
        if (task == null) {
            log.warn("回滚失败：任务不存在, taskId={}", taskId);
            return;
        }

        if (!MintTask.STATUS_FAILED.equals(task.getStepStatus())) {
            log.warn("回滚失败：任务状态不是失败, taskId={}, status={}", taskId, task.getStepStatus());
            return;
        }

        try {
            task.setRollbackStatus(MintTask.ROLLBACK_PENDING);
            task.setUpdatedAt(LocalDateTime.now());
            mintTaskMapper.updateById(task);

            if (task.getNftAssetId() != null) {
                QueryWrapper<NftVersion> versionWrapper = new QueryWrapper<>();
                versionWrapper.eq("nft_asset_id", task.getNftAssetId());
                nftVersionMapper.delete(versionWrapper);
                log.info("已删除NFT版本历史: nftAssetId={}", task.getNftAssetId());

                nftAssetMapper.deleteById(task.getNftAssetId());
                log.info("已删除NFT资产: nftAssetId={}", task.getNftAssetId());

                task.setNftAssetId(null);
                task.setTxHash(null);
            }

            task.setRollbackStatus(MintTask.ROLLBACK_COMPLETED);
            task.setUpdatedAt(LocalDateTime.now());
            mintTaskMapper.updateById(task);
            log.info("回滚完成: taskId={}", taskId);

        } catch (Exception e) {
            log.error("回滚失败: taskId={}, error={}", taskId, e.getMessage(), e);
            task.setRollbackStatus(MintTask.ROLLBACK_FAILED);
            task.setRollbackError(e.getMessage());
            task.setUpdatedAt(LocalDateTime.now());
            mintTaskMapper.updateById(task);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MintTask retryTask(String taskId) {
        MintTask task = getTask(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        if (!MintTask.STATUS_FAILED.equals(task.getStepStatus())) {
            throw new RuntimeException("只有失败的任务才能重试");
        }

        int retryCount = task.getRetryCount() != null ? task.getRetryCount() : 0;
        if (retryCount >= MintTask.MAX_RETRY_COUNT) {
            throw new RuntimeException("已达到最大重试次数: " + MintTask.MAX_RETRY_COUNT);
        }

        if (task.getNftAssetId() != null || MintTask.ROLLBACK_NONE.equals(task.getRollbackStatus())) {
            try {
                rollbackTask(taskId);
            } catch (Exception e) {
                log.warn("重试前回滚失败，继续重试: {}", e.getMessage());
            }
        }

        task.setCurrentStep(MintTask.STEP_UPLOAD);
        task.setStepStatus(MintTask.STATUS_PENDING);
        task.setProgressPercent(0);
        task.setErrorMessage(null);
        task.setRollbackStatus(MintTask.ROLLBACK_NONE);
        task.setRetryCount(retryCount + 1);
        task.setUpdatedAt(LocalDateTime.now());
        mintTaskMapper.updateById(task);

        task = getTask(taskId);
        executeMintTask(taskId);

        log.info("重试铸造任务: taskId={}, retryCount={}", taskId, retryCount + 1);
        return task;
    }

    @Override
    public void cancelTask(String taskId) {
        MintTask task = getTask(taskId);
        if (task == null) {
            log.warn("取消失败：任务不存在, taskId={}", taskId);
            return;
        }

        if (MintTask.STATUS_COMPLETED.equals(task.getStepStatus())) {
            log.warn("取消失败：已完成的任务不能取消, taskId={}", taskId);
            return;
        }

        task.setStepStatus(MintTask.STATUS_FAILED);
        task.setErrorMessage("用户取消");
        task.setUpdatedAt(LocalDateTime.now());
        mintTaskMapper.updateById(task);

        rollbackTask(taskId);
        log.info("任务已取消: taskId={}", taskId);
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void executeMintTask(String taskId) {
        MintTask task = getTask(taskId);
        if (task == null) {
            log.error("任务不存在: taskId={}", taskId);
            return;
        }

        try {
            // 步骤1: 上传已完成（文件已在 MinIO 中）
            updateProgress(taskId, MintTask.STEP_UPLOAD, MintTask.STATUS_COMPLETED, 25, null);

            // 步骤2: 生成元数据并上传到 IPFS
            updateProgress(taskId, MintTask.STEP_METADATA, MintTask.STATUS_PROCESSING, 30, null);
            String metadataCid = generateAndUploadMetadata(task);
            String metadataUrl = "ipfs://" + metadataCid;
            task.setMetadataUrl(metadataUrl);
            updateProgress(taskId, MintTask.STEP_METADATA, MintTask.STATUS_COMPLETED, 50, null);

            // 步骤3: 链上铸造
            updateProgress(taskId, MintTask.STEP_MINTING, MintTask.STATUS_PROCESSING, 60, null);

            // 模拟铸造过程（实际应调用智能合约）
            String txHash = "0x" + UUID.randomUUID().toString().replace("-", "");
            BigInteger tokenId = BigInteger.valueOf(System.currentTimeMillis());

            // 保存NFT资产
            NftAsset nftAsset = new NftAsset();
            nftAsset.setTokenId(tokenId.longValue());
            nftAsset.setContractAddress(contractProperties.getNftAssetAddress());
            nftAsset.setOwnerAddress(task.getCreatorAddress());
            nftAsset.setCreatorAddress(task.getCreatorAddress());
            nftAsset.setName(task.getName());
            nftAsset.setDescription(task.getDescription());
            nftAsset.setCategory(task.getCategory());
            nftAsset.setImageUrl(task.getImageUrl());
            nftAsset.setCurrentMetadataHash(metadataCid);
            nftAsset.setCurrentVersion(1);
            nftAsset.setRoyaltyFee(task.getRoyaltyFee());
            nftAsset.setRoyaltyRecipient(task.getCreatorAddress());
            nftAsset.setStatus(1);
            nftAsset.setReviewStatus(NftReview.STATUS_PENDING);
            nftAsset.setCreatedAt(LocalDateTime.now());
            nftAsset.setUpdatedAt(LocalDateTime.now());
            nftAssetMapper.insert(nftAsset);

            // 保存版本历史
            NftVersion version = new NftVersion();
            version.setNftAssetId(nftAsset.getId());
            version.setVersion(1);
            version.setMetadataHash(metadataCid);
            version.setIpfsUri(metadataUrl);
            version.setChangeDescription("初始版本");
            version.setUpdaterAddress(task.getCreatorAddress());
            version.setTxHash(txHash);
            version.setCreatedAt(LocalDateTime.now());
            nftVersionMapper.insert(version);

            updateProgress(taskId, MintTask.STEP_MINTING, MintTask.STATUS_COMPLETED, 90, null);

            // 步骤4: 完成
            completeTask(taskId, nftAsset.getId(), txHash);

            // 自动创建审核记录
            try {
                nftReviewService.submitForReview(nftAsset.getId());
                log.info("已自动提交审核: nftAssetId={}", nftAsset.getId());
            } catch (Exception e) {
                log.warn("自动提交审核失败: {}", e.getMessage());
            }

            log.info("铸造任务执行完成: taskId={}, nftAssetId={}, metadataCid={}", taskId, nftAsset.getId(), metadataCid);

        } catch (Exception e) {
            log.error("铸造任务执行失败: taskId={}, error={}", taskId, e.getMessage(), e);
            updateProgress(taskId, task.getCurrentStep(), MintTask.STATUS_FAILED, task.getProgressPercent(), e.getMessage());
        }
    }

    /**
     * 生成 NFT 元数据并上传到 IPFS
     * 1. 从 MinIO 读取已上传的图片文件
     * 2. 将图片上传到 IPFS 获取 imageCid
     * 3. 构建符合 ERC-721 标准的元数据 JSON
     * 4. 将元数据 JSON 上传到 IPFS 获取 metadataCid
     * 5. 返回 metadataCid
     */
    private String generateAndUploadMetadata(MintTask task) throws Exception {
        String imageCid;

        if (ipfsProperties.isDisabled()) {
            // IPFS 禁用时，使用 MinIO URL 作为图片引用
            imageCid = task.getImageUrl();
            log.info("IPFS disabled, using MinIO URL as image reference: {}", imageCid);
        } else {
            // 从 MinIO 读取图片文件并上传到 IPFS
            imageCid = uploadImageToIpfs(task);
        }

        // 构建符合 ERC-721 标准的元数据
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", task.getName());
        metadata.put("description", task.getDescription());

        // image 字段：真实 IPFS CID 使用 ipfs:// 协议，MinIO URL 直接使用
        if (imageCid.startsWith("Qm") || imageCid.startsWith("bafy") || imageCid.startsWith("bafk")) {
            metadata.put("image", "ipfs://" + imageCid);
        } else {
            metadata.put("image", imageCid);
        }

        // 添加分类属性
        if (task.getCategory() != null) {
            List<Map<String, String>> attributes = new ArrayList<>();
            Map<String, String> categoryAttr = new LinkedHashMap<>();
            categoryAttr.put("trait_type", "Category");
            categoryAttr.put("value", task.getCategory());
            attributes.add(categoryAttr);
            metadata.put("attributes", attributes);
        }

        metadata.put("created_at", LocalDateTime.now().toString());

        // 上传元数据 JSON 到 IPFS
        String metadataCid;
        if (ipfsProperties.isDisabled()) {
            // IPFS 禁用时，使用 IpfsServiceEnhanced（存到 MinIO）
            metadataCid = ipfsServiceEnhanced.uploadMetadata(metadata);
            log.info("IPFS disabled, metadata stored in MinIO: {}", metadataCid);
        } else {
            // 上传到真实 IPFS
            metadataCid = ipfsService.uploadMetadata(metadata);
            log.info("Metadata uploaded to IPFS, CID: {}", metadataCid);
        }

        return metadataCid;
    }

    /**
     * 从 MinIO 读取图片文件并上传到 IPFS
     */
    private String uploadImageToIpfs(MintTask task) throws Exception {
        String imageUrl = task.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new RuntimeException("图片 URL 为空，无法上传到 IPFS");
        }

        // 从 MinIO 读取图片
        String objectName = extractMinioObjectName(imageUrl);
        byte[] imageData;
        try {
            InputStream stream = minioService.getFile(objectName);
            imageData = stream.readAllBytes();
            stream.close();
            log.info("从 MinIO 读取图片: {}, 大小: {} bytes", objectName, imageData.length);
        } catch (Exception e) {
            throw new RuntimeException("从 MinIO 读取图片失败: " + e.getMessage(), e);
        }

        // 上传到 IPFS
        String filename = task.getName() != null ? task.getName().replaceAll("[^a-zA-Z0-9.-]", "_") : "image";
        String imageCid = ipfsService.uploadBytes(imageData, filename);
        log.info("图片上传到 IPFS 成功: {} -> CID: {}", filename, imageCid);

        return imageCid;
    }

    /**
     * 从 URL 中提取 MinIO 对象名
     * 支持格式: /minio/bucket/objectName, http://host:port/bucket/objectName, 或纯 objectName
     */
    private String extractMinioObjectName(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // /minio/bucket/objectName 格式
        if (url.startsWith("/minio/")) {
            String path = url.substring("/minio/".length());
            int slashIndex = path.indexOf('/');
            if (slashIndex > 0) {
                return path.substring(slashIndex + 1);
            }
            return path;
        }

        // http:// 或 https:// 格式
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // 提取路径部分
            int pathStart = url.indexOf('/', url.indexOf("//") + 2);
            if (pathStart > 0) {
                String path = url.substring(pathStart + 1);
                // 跳过 bucket 名
                int slashIndex = path.indexOf('/');
                if (slashIndex > 0) {
                    return path.substring(slashIndex + 1);
                }
                return path;
            }
        }

        // 已经是纯 objectName
        return url;
    }
}