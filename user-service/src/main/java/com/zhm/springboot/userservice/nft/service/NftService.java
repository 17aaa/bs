package com.zhm.springboot.userservice.nft.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhm.springboot.userservice.blockchain.config.ContractProperties;
import com.zhm.springboot.userservice.audit.entity.AuditLog;
import com.zhm.springboot.userservice.audit.service.AuditLogService;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftVersion;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.nft.mapper.NftVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * NFT 业务服务
 * 处理 NFT 铸造、元数据更新、版本控制等核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NftService {

    private final NftAssetMapper nftAssetMapper;
    private final NftVersionMapper nftVersionMapper;
    private final ContractProperties contractProperties;
    private final AuditLogService auditLogService;

    /**
     * 铸造 NFT
     *
     * @param creatorAddress 创作者地址
     * @param name           作品名称
     * @param description    作品描述
     * @param category       分类
     * @param metadataUrl    元数据/图片 URL（MinIO）
     * @param royaltyFee     版税比例（万分比）
     * @return NFT 资产 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long mintNft(String creatorAddress, String name, String description,
                        String category, String metadataUrl, Integer royaltyFee) throws Exception {
        // 1. 生成元数据哈希（SHA-256，用于链上标识）
        String metadataHash = generateMetadataHash(name, description, category);
        log.info("Generated metadata hash: {}", metadataHash);

        // 2. 持久化 NFT 资产
        NftAsset nftAsset = new NftAsset();
        nftAsset.setContractAddress(contractProperties.getNftAssetAddress());
        nftAsset.setOwnerAddress(creatorAddress);
        nftAsset.setCreatorAddress(creatorAddress);
        nftAsset.setName(name);
        nftAsset.setDescription(description);
        nftAsset.setCategory(category);
        nftAsset.setImageUrl(metadataUrl);
        nftAsset.setCurrentMetadataHash(metadataHash);
        nftAsset.setCurrentVersion(1);
        nftAsset.setRoyaltyFee(royaltyFee != null ? royaltyFee : 500);
        nftAsset.setRoyaltyRecipient(creatorAddress);
        nftAsset.setStatus(1);
        nftAsset.setReviewStatus("pending");
        nftAsset.setCreatedAt(LocalDateTime.now());
        nftAsset.setUpdatedAt(LocalDateTime.now());

        nftAssetMapper.insert(nftAsset);

        // 3. 使用数据库自增 ID 作为 Token ID（全局唯一，单调递增）
        Long tokenId = nftAsset.getId();
        nftAsset.setTokenId(tokenId);
        // 生成与此 NFT 对应的确定性交易哈希
        String txHash = generateTxHash(tokenId);
        nftAssetMapper.updateById(nftAsset);

        log.info("NFT minted: id={}, tokenId={}, txHash={}", nftAsset.getId(), tokenId, txHash);

        auditLogService.log(null, AuditLog.ACTION_MINT, AuditLog.MODULE_NFT,
                "铸造NFT: " + name + ", tokenId=" + tokenId);

        // 4. 记录初始版本历史
        NftVersion version = new NftVersion();
        version.setNftAssetId(nftAsset.getId());
        version.setVersion(1);
        version.setMetadataHash(metadataHash);
        version.setIpfsUri(metadataUrl != null ? metadataUrl : metadataHash);
        version.setChangeDescription("初始版本");
        version.setUpdaterAddress(creatorAddress);
        version.setTxHash(txHash);
        version.setBlockNumber(null);
        version.setCreatedAt(LocalDateTime.now());

        nftVersionMapper.insert(version);

        return nftAsset.getId();
    }

    /**
     * 更新 NFT 元数据（版本控制）
     *
     * @param nftAssetId     NFT 资产 ID
     * @param name           新名称（可选）
     * @param description    新描述（可选）
     * @param changeDesc     变更描述
     * @param updaterAddress 更新者地址
     * @return 新版本号
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateMetadata(Long nftAssetId, String name, String description,
                                   String changeDesc, String updaterAddress) throws Exception {
        NftAsset nftAsset = nftAssetMapper.selectById(nftAssetId);
        if (nftAsset == null) {
            throw new IllegalArgumentException("NFT asset not found: " + nftAssetId);
        }

        if (!nftAsset.getCreatorAddress().equalsIgnoreCase(updaterAddress) &&
                !nftAsset.getOwnerAddress().equalsIgnoreCase(updaterAddress)) {
            throw new SecurityException("Only creator or owner can update metadata");
        }

        // 生成新元数据哈希
        String newMetadataHash = generateMetadataHash(
                name != null ? name : nftAsset.getName(),
                description != null ? description : nftAsset.getDescription(),
                nftAsset.getCategory()
        );

        int newVersion = nftAsset.getCurrentVersion() + 1;
        String txHash = generateTxHash(nftAsset.getTokenId() * 1000L + newVersion);

        // 更新资产信息
        nftAsset.setCurrentMetadataHash(newMetadataHash);
        nftAsset.setCurrentVersion(newVersion);
        nftAsset.setUpdatedAt(LocalDateTime.now());
        if (name != null) nftAsset.setName(name);
        if (description != null) nftAsset.setDescription(description);

        nftAssetMapper.updateById(nftAsset);

        // 记录版本历史
        NftVersion version = new NftVersion();
        version.setNftAssetId(nftAssetId);
        version.setVersion(newVersion);
        version.setMetadataHash(newMetadataHash);
        version.setIpfsUri(nftAsset.getImageUrl() != null ? nftAsset.getImageUrl() : newMetadataHash);
        version.setChangeDescription(changeDesc != null ? changeDesc : "版本更新");
        version.setUpdaterAddress(updaterAddress);
        version.setTxHash(txHash);
        version.setCreatedAt(LocalDateTime.now());

        nftVersionMapper.insert(version);

        log.info("NFT metadata updated: id={}, newVersion={}", nftAssetId, newVersion);

        auditLogService.log(null, AuditLog.ACTION_UPDATE, AuditLog.MODULE_NFT,
                "更新NFT元数据: id=" + nftAssetId + ", version=" + newVersion);
        return newVersion;
    }

    /** 获取 NFT 详情 */
    public NftAsset getNftAsset(Long id) {
        return nftAssetMapper.selectById(id);
    }

    /** 获取 NFT 版本历史 */
    public List<NftVersion> getVersionHistory(Long nftAssetId) {
        LambdaQueryWrapper<NftVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NftVersion::getNftAssetId, nftAssetId);
        wrapper.orderByDesc(NftVersion::getVersion);
        return nftVersionMapper.selectList(wrapper);
    }

    /** 获取用户持有的 NFT */
    public List<NftAsset> getNftsByOwner(String ownerAddress) {
        LambdaQueryWrapper<NftAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NftAsset::getOwnerAddress, ownerAddress);
        wrapper.eq(NftAsset::getStatus, 1);
        wrapper.orderByDesc(NftAsset::getCreatedAt);
        return nftAssetMapper.selectList(wrapper);
    }

    /** 获取用户创作的 NFT */
    public List<NftAsset> getNftsByCreator(String creatorAddress) {
        LambdaQueryWrapper<NftAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NftAsset::getCreatorAddress, creatorAddress);
        wrapper.orderByDesc(NftAsset::getCreatedAt);
        return nftAssetMapper.selectList(wrapper);
    }

    /** 获取所有 NFT（分页） */
    public List<NftAsset> getAllNfts(Integer limit, Integer offset) {
        LambdaQueryWrapper<NftAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NftAsset::getStatus, 1);
        wrapper.orderByDesc(NftAsset::getCreatedAt);
        return nftAssetMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(offset, limit),
                wrapper
        ).getRecords();
    }

    /**
     * 生成元数据哈希（SHA-256）
     * 将 NFT 核心属性组合后哈希，作为内容指纹存储于版本历史中
     */
    private String generateMetadataHash(String name, String description, String category) throws Exception {
        String content = String.format("%s|%s|%s|%d",
                name != null ? name : "",
                description != null ? description : "",
                category != null ? category : "",
                System.currentTimeMillis());

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 根据 ID 生成确定性的交易哈希（64 位 hex，用于系统内标识）
     */
    private String generateTxHash(Long id) {
        return "0x" + String.format("%064x", id);
    }
}
