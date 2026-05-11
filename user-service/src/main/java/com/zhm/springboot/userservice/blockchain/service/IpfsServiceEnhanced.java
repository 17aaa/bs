package com.zhm.springboot.userservice.blockchain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhm.springboot.userservice.blockchain.config.IpfsProperties;
import com.zhm.springboot.userservice.blockchain.dto.*;
import com.zhm.springboot.userservice.blockchain.entity.IpfsFileRecord;
import com.zhm.springboot.userservice.blockchain.exception.IpfsException;
import com.zhm.springboot.userservice.blockchain.mapper.IpfsFileRecordMapper;
import com.zhm.springboot.userservice.config.MinIOProperties;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 增强版文件存储服务
 * 上传文件时同时存储到 MinIO（快速预览）和 IPFS（不可变存证）
 * MinIO 作为上传缓冲层提供快速访问，IPFS 提供内容寻址的不可变存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpfsServiceEnhanced {

    private final MinioClient minioClient;
    private final MinIOProperties minioProperties;
    private final IpfsProperties ipfsProperties;
    private final IpfsService ipfsService;
    private final IpfsFileRecordMapper ipfsFileRecordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 文件上传 ====================

    @Transactional
    public IpfsUploadResponse uploadFile(MultipartFile file, IpfsUploadRequest request, Long userId) {
        log.info("Uploading file: {}, userId: {}, ipfsMode: {}", file.getOriginalFilename(), userId, ipfsProperties.getMode());

        validateFile(file);

        String minioKey = null;
        String ipfsCid = null;

        try {
            // Step 1: 上传到 MinIO（快速预览）
            minioKey = uploadToMinio(file);
            log.info("File uploaded to MinIO: {}", minioKey);

            // Step 2: 上传到 IPFS（不可变存证）
            if (!ipfsProperties.isDisabled()) {
                try {
                    ipfsCid = ipfsService.uploadBytes(file.getBytes(), file.getOriginalFilename());
                    log.info("File uploaded to IPFS: {} -> CID: {}", file.getOriginalFilename(), ipfsCid);
                } catch (Exception e) {
                    log.warn("IPFS upload failed, file stored in MinIO only: {}", e.getMessage());
                }
            }

            // 使用 IPFS CID 作为主标识，MinIO key 作为备用
            String primaryCid = ipfsCid != null ? ipfsCid : minioKey;

            IpfsFileRecord record = saveFileRecord(file, primaryCid, minioKey, ipfsCid, request, userId);
            String accessUrl = getGatewayUrl(primaryCid);

            return IpfsUploadResponse.builder()
                    .cid(primaryCid)
                    .filename(file.getOriginalFilename())
                    .size(file.getSize())
                    .mimeType(file.getContentType())
                    .gatewayUrl(accessUrl)
                    .ipfsUrl(ipfsCid != null ? "ipfs://" + ipfsCid : accessUrl)
                    .pinned(ipfsCid != null)
                    .uploadedAt(record.getCreatedAt())
                    .status("success")
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new IpfsException("上传文件失败: " + e.getMessage(), IpfsException.ErrorCode.UPLOAD_FAILED, e);
        }
    }

    @Transactional
    public IpfsBatchUploadResponse uploadFiles(List<MultipartFile> files, IpfsUploadRequest request, Long userId) {
        log.info("Batch uploading {} files", files.size());

        if (files.size() > ipfsProperties.getMaxBatchSize()) {
            throw new IpfsException("批量上传文件数超过限制: " + ipfsProperties.getMaxBatchSize(),
                    IpfsException.ErrorCode.UPLOAD_FAILED);
        }

        List<IpfsUploadResponse> successful = new ArrayList<>();
        List<IpfsUploadResponse> failed = new ArrayList<>();
        long totalSize = 0;

        for (MultipartFile file : files) {
            try {
                IpfsUploadResponse response = uploadFile(file, request, userId);
                successful.add(response);
                totalSize += response.getSize();
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                failed.add(IpfsUploadResponse.builder()
                        .filename(file.getOriginalFilename())
                        .size(file.getSize())
                        .status("failed")
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        return IpfsBatchUploadResponse.builder()
                .successful(successful)
                .failed(failed)
                .totalCount(files.size())
                .successCount(successful.size())
                .failedCount(failed.size())
                .totalSize(totalSize)
                .build();
    }

    public String uploadBytes(byte[] data, String filename) throws IOException {
        if (ipfsProperties.isDisabled()) {
            return uploadBytesToMinio(data, filename);
        }

        // 上传到 IPFS
        try {
            String cid = ipfsService.uploadBytes(data, filename);
            log.info("Bytes uploaded to IPFS: {} -> CID: {}", filename, cid);
            return cid;
        } catch (Exception e) {
            log.warn("IPFS upload failed, falling back to MinIO: {}", e.getMessage());
            return uploadBytesToMinio(data, filename);
        }
    }

    public String uploadMetadata(Object metadata) throws IOException {
        if (ipfsProperties.isDisabled()) {
            return uploadMetadataToMinio(metadata);
        }

        // 元数据上传到 IPFS（NFT 标准要求 ipfs:// URI）
        try {
            String cid = ipfsService.uploadMetadata(metadata);
            log.info("Metadata uploaded to IPFS: CID: {}", cid);
            return cid;
        } catch (Exception e) {
            log.warn("IPFS metadata upload failed, falling back to MinIO: {}", e.getMessage());
            return uploadMetadataToMinio(metadata);
        }
    }

    // ==================== 文件固定 ====================

    public boolean pinFile(String cid) {
        if (ipfsProperties.isDisabled()) {
            log.info("IPFS disabled, pin is no-op: {}", cid);
            try {
                ipfsFileRecordMapper.updatePinStatus(cid, true);
            } catch (Exception e) {
                log.warn("Failed to update pin status for {}", cid);
            }
            return true;
        }

        try {
            boolean result = ipfsService.pinByHash(cid);
            if (result) {
                ipfsFileRecordMapper.updatePinStatus(cid, true);
            }
            return result;
        } catch (IOException e) {
            log.error("Failed to pin file on IPFS: {}", cid, e);
            // Still update DB status for tracking
            try {
                ipfsFileRecordMapper.updatePinStatus(cid, true);
            } catch (Exception ex) {
                log.warn("Failed to update pin status for {}", cid);
            }
            return false;
        }
    }

    public boolean unpinFile(String cid) {
        if (ipfsProperties.isDisabled()) {
            log.info("IPFS disabled, unpin is no-op: {}", cid);
            try {
                ipfsFileRecordMapper.updatePinStatus(cid, false);
            } catch (Exception e) {
                log.warn("Failed to update pin status for {}", cid);
            }
            return true;
        }

        try {
            boolean result = ipfsService.unpinByHash(cid);
            if (result) {
                ipfsFileRecordMapper.updatePinStatus(cid, false);
            }
            return result;
        } catch (IOException e) {
            log.error("Failed to unpin file on IPFS: {}", cid, e);
            try {
                ipfsFileRecordMapper.updatePinStatus(cid, false);
            } catch (Exception ex) {
                log.warn("Failed to update pin status for {}", cid);
            }
            return false;
        }
    }

    public List<String> getPinnedFiles() {
        LambdaQueryWrapper<IpfsFileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpfsFileRecord::getPinned, true);
        return ipfsFileRecordMapper.selectList(wrapper)
                .stream().map(IpfsFileRecord::getCid).collect(Collectors.toList());
    }

    // ==================== 文件查询 ====================

    public IpfsFileRecord getFileByCid(String cid) {
        return ipfsFileRecordMapper.selectByCid(cid);
    }

    public Page<IpfsFileRecord> queryFiles(IpfsFileQuery query) {
        LambdaQueryWrapper<IpfsFileRecord> wrapper = new LambdaQueryWrapper<>();

        if (query.getUserId() != null) {
            wrapper.eq(IpfsFileRecord::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getOwnerAddress())) {
            wrapper.eq(IpfsFileRecord::getOwnerAddress, query.getOwnerAddress());
        }
        if (StringUtils.hasText(query.getFileType())) {
            wrapper.eq(IpfsFileRecord::getFileType, query.getFileType());
        }
        if (query.getFileTypes() != null && !query.getFileTypes().isEmpty()) {
            wrapper.in(IpfsFileRecord::getFileType, query.getFileTypes());
        }
        if (query.getPinned() != null) {
            wrapper.eq(IpfsFileRecord::getPinned, query.getPinned());
        }
        if (query.getNftAssetId() != null) {
            wrapper.eq(IpfsFileRecord::getNftAssetId, query.getNftAssetId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(IpfsFileRecord::getFilename, query.getKeyword())
                    .or().like(IpfsFileRecord::getDescription, query.getKeyword()));
        }
        if (query.getStartTime() != null) {
            wrapper.ge(IpfsFileRecord::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(IpfsFileRecord::getCreatedAt, query.getEndTime());
        }

        if (Boolean.TRUE.equals(query.getDesc())) {
            wrapper.orderByDesc(IpfsFileRecord::getCreatedAt);
        } else {
            wrapper.orderByAsc(IpfsFileRecord::getCreatedAt);
        }

        Page<IpfsFileRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        return ipfsFileRecordMapper.selectPage(page, wrapper);
    }

    public List<IpfsFileRecord> getFilesByUserId(Long userId) {
        return ipfsFileRecordMapper.selectByUserId(userId);
    }

    public List<IpfsFileRecord> getFilesByOwnerAddress(String ownerAddress) {
        return ipfsFileRecordMapper.selectByOwnerAddress(ownerAddress);
    }

    // ==================== 文件下载 ====================

    public byte[] downloadFile(String cid) {
        // 优先从 IPFS 网关下载（如果是真实 CID）
        if (isRealIpfsCid(cid) && !ipfsProperties.isDisabled()) {
            try {
                byte[] data = ipfsService.downloadFile(cid);
                log.info("Downloaded file from IPFS: {}", cid);
                return data;
            } catch (Exception e) {
                log.warn("IPFS download failed, trying MinIO: {}", e.getMessage());
            }
        }

        // 从 MinIO 下载
        log.info("Downloading file from MinIO: {}", cid);
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(cid)
                    .build());
            byte[] data = stream.readAllBytes();
            stream.close();
            incrementAccessCount(cid);
            return data;
        } catch (Exception e) {
            log.error("Failed to download file: {}", cid, e);
            throw new IpfsException("无法下载文件: " + cid, IpfsException.ErrorCode.DOWNLOAD_FAILED);
        }
    }

    public byte[] getFileContent(String objectKey) {
        return downloadFile(objectKey);
    }

    // ==================== 统计信息 ====================

    public IpfsStatistics getStatistics() {
        Map<String, Object> totalStats = ipfsFileRecordMapper.selectTotalStatistics();

        long totalFiles = ((Number) totalStats.getOrDefault("total_files", 0)).longValue();
        long totalSize = ((Number) totalStats.getOrDefault("total_size", 0)).longValue();
        long totalAccess = ((Number) totalStats.getOrDefault("total_access", 0)).longValue();

        List<Map<String, Object>> typeStats = ipfsFileRecordMapper.selectStatisticsByType();
        Map<String, IpfsStatistics.TypeStatistics> byType = new HashMap<>();

        for (Map<String, Object> stat : typeStats) {
            String fileType = (String) stat.get("file_type");
            long count = ((Number) stat.getOrDefault("count", 0)).longValue();
            long size = ((Number) stat.getOrDefault("total_size", 0)).longValue();
            double percentage = totalFiles > 0 ? (count * 100.0 / totalFiles) : 0;

            byType.put(fileType, IpfsStatistics.TypeStatistics.builder()
                    .count(count).totalSize(size).percentage(percentage).build());
        }

        LambdaQueryWrapper<IpfsFileRecord> pinnedWrapper = new LambdaQueryWrapper<>();
        pinnedWrapper.eq(IpfsFileRecord::getPinned, true);
        long pinnedCount = ipfsFileRecordMapper.selectCount(pinnedWrapper);

        return IpfsStatistics.builder()
                .totalFiles(totalFiles)
                .totalSize(totalSize)
                .totalAccessCount(totalAccess)
                .pinnedFiles(pinnedCount)
                .unpinnedFiles(totalFiles - pinnedCount)
                .byType(byType)
                .averageFileSize(totalFiles > 0 ? totalSize / totalFiles : 0)
                .build();
    }

    // ==================== 工具方法 ====================

    public String getGatewayUrl(String cid) {
        if (cid == null || cid.isEmpty()) {
            return "";
        }
        // 真实 IPFS CID（Qm... 或 bafy.../bafk...）使用 IPFS 网关
        if (isRealIpfsCid(cid)) {
            String cleanCid = cid.startsWith("ipfs://") ? cid.substring(7) : cid;
            return ipfsProperties.getGateway() + cleanCid;
        }
        // MinIO object key 使用 MinIO URL
        String baseUrl = minioProperties.getPublicAccessUrl();
        if (baseUrl != null && !baseUrl.isEmpty()) {
            if (!baseUrl.endsWith("/")) baseUrl += "/";
            return baseUrl + minioProperties.getBucketName() + "/" + cid;
        }
        return "/minio/" + minioProperties.getBucketName() + "/" + cid;
    }

    public List<String> getBackupGatewayUrls(String cid) {
        if (isRealIpfsCid(cid)) {
            String cleanCid = cid.startsWith("ipfs://") ? cid.substring(7) : cid;
            List<String> urls = new ArrayList<>();
            for (String gateway : ipfsProperties.getBackupGateways()) {
                urls.add(gateway + cleanCid);
            }
            return urls;
        }
        return Collections.singletonList(getGatewayUrl(cid));
    }

    @Transactional
    public boolean deleteFileRecord(String objectKey) {
        log.info("Deleting file record: {}", objectKey);
        LambdaQueryWrapper<IpfsFileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpfsFileRecord::getCid, objectKey);
        IpfsFileRecord record = new IpfsFileRecord();
        record.setStatus("deleted");
        return ipfsFileRecordMapper.update(record, wrapper) > 0;
    }

    @Transactional
    public boolean updateFileInfo(String objectKey, String description, String tags) {
        LambdaQueryWrapper<IpfsFileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IpfsFileRecord::getCid, objectKey);
        IpfsFileRecord record = new IpfsFileRecord();
        record.setDescription(description);
        record.setTags(tags);
        return ipfsFileRecordMapper.update(record, wrapper) > 0;
    }

    // ==================== 私有方法 ====================

    /**
     * 判断是否为真实 IPFS CID
     * CIDv0: Qm 开头，长度 46
     * CIDv1: bafy/bafk/bafyrei 等开头
     */
    private boolean isRealIpfsCid(String cid) {
        if (cid == null || cid.isEmpty()) return false;
        String clean = cid.startsWith("ipfs://") ? cid.substring(7) : cid;
        return (clean.startsWith("Qm") && clean.length() >= 46)
                || clean.startsWith("bafy") || clean.startsWith("bafk") || clean.startsWith("bafyrei");
    }

    private String uploadToMinio(MultipartFile file) throws Exception {
        String objectKey = generateObjectKey(file.getOriginalFilename());
        ensureBucket();

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(objectKey)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        return objectKey;
    }

    private String uploadBytesToMinio(byte[] data, String filename) {
        try {
            String objectKey = generateObjectKey(filename);
            ensureBucket();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType("application/octet-stream")
                    .build());

            log.info("Bytes uploaded to MinIO: {} -> {}", filename, objectKey);
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed: " + e.getMessage(), e);
        }
    }

    private String uploadMetadataToMinio(Object metadata) {
        try {
            String json = objectMapper.writeValueAsString(metadata);
            byte[] data = json.getBytes("UTF-8");
            String objectKey = "metadata/" + UUID.randomUUID() + ".json";
            ensureBucket();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType("application/json")
                    .build());

            log.info("Metadata uploaded to MinIO: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("MinIO metadata upload failed: " + e.getMessage(), e);
        }
    }

    private String generateObjectKey(String filename) {
        String ext = "";
        if (filename != null && filename.contains(".")) {
            ext = filename.substring(filename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + ext;
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
                String policy = String.format(
                        "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\"," +
                        "\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                        minioProperties.getBucketName());
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .config(policy)
                        .build());
                log.info("Created MinIO bucket: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.warn("Failed to ensure bucket: {}", e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > ipfsProperties.getMaxFileSize()) {
            throw new IpfsException("文件大小超过限制: " + formatFileSize(ipfsProperties.getMaxFileSize()),
                    IpfsException.ErrorCode.FILE_TOO_LARGE);
        }
        String mimeType = file.getContentType();
        if (!ipfsProperties.getAllowedMimeTypes().contains(mimeType)) {
            throw new IpfsException("不支持的文件类型: " + mimeType, IpfsException.ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private IpfsFileRecord saveFileRecord(MultipartFile file, String primaryCid,
                                           String minioKey, String ipfsCid,
                                           IpfsUploadRequest request, Long userId) {
        IpfsFileRecord record = new IpfsFileRecord();
        record.setCid(primaryCid);
        record.setFilename(file.getOriginalFilename());
        record.setFileSize(file.getSize());
        record.setMimeType(file.getContentType());
        record.setFileType(detectFileType(file.getContentType()));
        record.setPinned(ipfsCid != null);
        record.setPinnedAt(ipfsCid != null ? LocalDateTime.now() : null);
        record.setUserId(userId);
        record.setStatus("active");
        record.setAccessCount(0);

        if (request != null) {
            record.setDescription(request.getDescription());
            record.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
            record.setNftAssetId(request.getNftAssetId());
            if (request.getMetadata() != null && request.getMetadata().containsKey("ownerAddress")) {
                record.setOwnerAddress((String) request.getMetadata().get("ownerAddress"));
            }
        }

        ipfsFileRecordMapper.insert(record);
        return record;
    }

    private String detectFileType(String mimeType) {
        if (mimeType == null) return "other";
        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        if (mimeType.equals("application/json")) return "json";
        if (mimeType.startsWith("text/")) return "text";
        return "other";
    }

    private void incrementAccessCount(String objectKey) {
        try {
            ipfsFileRecordMapper.incrementAccessCount(objectKey);
        } catch (Exception e) {
            log.warn("Failed to increment access count for {}", objectKey);
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}