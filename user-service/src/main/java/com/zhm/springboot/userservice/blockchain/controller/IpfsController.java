package com.zhm.springboot.userservice.blockchain.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhm.springboot.userservice.blockchain.config.IpfsProperties;
import com.zhm.springboot.userservice.blockchain.dto.*;
import com.zhm.springboot.userservice.blockchain.entity.IpfsFileRecord;
import com.zhm.springboot.userservice.blockchain.service.IpfsService;
import com.zhm.springboot.userservice.blockchain.service.IpfsServiceEnhanced;
import com.zhm.springboot.userservice.blockchain.service.NftMetadataService;
import com.zhm.springboot.userservice.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IPFS 存储控制器（增强版）
 * 提供完整的文件上传、下载、固定、查询、统计等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/ipfs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IpfsController {

    private final IpfsService ipfsService;
    private final IpfsServiceEnhanced ipfsServiceEnhanced;
    private final NftMetadataService nftMetadataService;
    private final IpfsProperties ipfsProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 文件上传 ====================

    /**
     * 上传单个文件到 IPFS
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<IpfsUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "pin", required = false, defaultValue = "true") Boolean pin,
            @RequestAttribute(value = "userId", required = false) Long userId) {

        log.info("Received file upload request: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }

        try {
            IpfsUploadRequest request = new IpfsUploadRequest();
            request.setDescription(description);
            if (tags != null && !tags.isEmpty()) {
                request.setTags(List.of(tags.split(",")));
            }
            request.setPin(pin);

            IpfsUploadResponse response = ipfsServiceEnhanced.uploadFile(file, request, userId);
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Failed to upload file to IPFS", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传图片到 IPFS（兼容旧接口）
     * IPFS 模式下上传到真实 IPFS，disabled 模式下降级到 MinIO
     */
    @PostMapping("/upload/image")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("Received image upload request: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }

        try {
            String cid;
            String gatewayUrl;

            if (ipfsProperties.isDisabled()) {
                // IPFS 禁用时降级到 MinIO
                IpfsUploadResponse response = ipfsServiceEnhanced.uploadFile(file, null, null);
                cid = response.getCid();
                gatewayUrl = response.getGatewayUrl();
            } else {
                // 保存到临时文件后上传到 IPFS
                java.io.File tempFile = java.io.File.createTempFile("ipfs_", "_" + file.getOriginalFilename());
                file.transferTo(tempFile);

                cid = ipfsService.uploadFile(tempFile);
                gatewayUrl = ipfsService.getGatewayUrl(cid);

                tempFile.delete();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("cid", cid);
            result.put("url", gatewayUrl);
            result.put("filename", file.getOriginalFilename());
            result.put("size", file.getSize());

            log.info("Image uploaded successfully. CID: {}", cid);
            return ApiResponse.success(result);

        } catch (IOException e) {
            log.error("Failed to upload image to IPFS", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<IpfsBatchUploadResponse> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "pin", required = false, defaultValue = "true") Boolean pin,
            @RequestAttribute(value = "userId", required = false) Long userId) {

        log.info("Received batch upload request: {} files", files.size());

        if (files.isEmpty()) {
            return ApiResponse.error("文件列表为空");
        }

        try {
            IpfsUploadRequest request = new IpfsUploadRequest();
            request.setDescription(description);
            if (tags != null && !tags.isEmpty()) {
                request.setTags(List.of(tags.split(",")));
            }
            request.setPin(pin);

            IpfsBatchUploadResponse response = ipfsServiceEnhanced.uploadFiles(files, request, userId);
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Failed to batch upload files", e);
            return ApiResponse.error("批量上传失败: " + e.getMessage());
        }
    }

    // ==================== NFT 元数据 ====================

    /**
     * 上传 JSON 元数据到 IPFS
     * IPFS 模式下上传到真实 IPFS，disabled 模式下降级到 MinIO
     */
    @PostMapping("/upload/metadata")
    public ApiResponse<Map<String, Object>> uploadMetadata(@RequestBody Map<String, Object> metadata) {
        log.info("Received metadata upload request");

        try {
            String cid;
            String gatewayUrl;

            if (ipfsProperties.isDisabled()) {
                // IPFS 禁用时降级到 MinIO
                cid = ipfsServiceEnhanced.uploadMetadata(metadata);
                gatewayUrl = ipfsServiceEnhanced.getGatewayUrl(cid);
            } else {
                cid = ipfsService.uploadMetadata(metadata);
                gatewayUrl = ipfsService.getGatewayUrl(cid);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("cid", cid);
            result.put("url", gatewayUrl);

            log.info("Metadata uploaded to IPFS successfully. CID: {}", cid);
            return ApiResponse.success(result);

        } catch (IOException e) {
            log.error("Failed to upload metadata to IPFS", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 创建并上传 NFT 元数据（包含图片）
     */
    @PostMapping(value = "/nft/metadata", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> createNftMetadata(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "attributes", required = false) String attributesJson,
            @RequestAttribute(value = "userId", required = false) Long userId) {

        log.info("Creating NFT metadata: {}", name);

        try {
            // 解析属性
            List<NftMetadataService.MetadataAttribute> attributes = new ArrayList<>();
            if (attributesJson != null && !attributesJson.isEmpty()) {
                attributes = objectMapper.readValue(attributesJson, new TypeReference<List<NftMetadataService.MetadataAttribute>>() {});
            }

            // 创建元数据
            NftMetadataService.NftMetadataResult result = nftMetadataService.createNftMetadata(
                    name, description, image, attributes, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("imageCid", result.getImageCid());
            response.put("imageUrl", result.getImageUrl());
            response.put("metadataCid", result.getMetadataCid());
            response.put("metadataUrl", result.getMetadataUrl());
            response.put("name", result.getName());
            response.put("description", result.getDescription());

            log.info("NFT metadata created successfully. Metadata CID: {}", result.getMetadataCid());
            return ApiResponse.success(response);

        } catch (IOException e) {
            log.error("Failed to create NFT metadata", e);
            return ApiResponse.error("创建元数据失败: " + e.getMessage());
        }
    }

    /**
     * 使用已上传的图片创建 NFT 元数据
     */
    @PostMapping("/nft/metadata/with-image")
    public ApiResponse<Map<String, Object>> createNftMetadataWithImage(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("imageCid") String imageCid,
            @RequestBody(required = false) List<Map<String, Object>> attributes) {

        log.info("Creating NFT metadata with existing image: {}", name);

        try {
            List<NftMetadataService.MetadataAttribute> attrs = new ArrayList<>();
            if (attributes != null) {
                for (Map<String, Object> attr : attributes) {
                    attrs.add(NftMetadataService.MetadataAttribute.builder()
                            .traitType((String) attr.get("trait_type"))
                            .value((String) attr.get("value"))
                            .displayType((String) attr.get("display_type"))
                            .build());
                }
            }

            NftMetadataService.NftMetadataResult result = nftMetadataService.createNftMetadataWithExistingImage(
                    name, description, imageCid, attrs);

            Map<String, Object> response = new HashMap<>();
            response.put("imageCid", result.getImageCid());
            response.put("imageUrl", result.getImageUrl());
            response.put("metadataCid", result.getMetadataCid());
            response.put("metadataUrl", result.getMetadataUrl());

            return ApiResponse.success(response);

        } catch (IOException e) {
            log.error("Failed to create NFT metadata", e);
            return ApiResponse.error("创建元数据失败: " + e.getMessage());
        }
    }

    // ==================== 文件固定 ====================

    /**
     * 固定文件
     */
    @PostMapping("/pin/{cid}")
    public ApiResponse<Map<String, Object>> pinFile(@PathVariable String cid) {
        log.info("Pinning file: {}", cid);

        boolean success = ipfsServiceEnhanced.pinFile(cid);

        Map<String, Object> result = new HashMap<>();
        result.put("cid", cid);
        result.put("pinned", success);

        if (success) {
            return ApiResponse.success(result, "文件固定成功");
        } else {
            return ApiResponse.error("文件固定失败");
        }
    }

    /**
     * 取消固定文件
     */
    @PostMapping("/unpin/{cid}")
    public ApiResponse<Map<String, Object>> unpinFile(@PathVariable String cid) {
        log.info("Unpinning file: {}", cid);

        boolean success = ipfsServiceEnhanced.unpinFile(cid);

        Map<String, Object> result = new HashMap<>();
        result.put("cid", cid);
        result.put("pinned", !success);

        if (success) {
            return ApiResponse.success(result, "文件取消固定成功");
        } else {
            return ApiResponse.error("文件取消固定失败");
        }
    }

    /**
     * 获取固定列表
     */
    @GetMapping("/pins")
    public ApiResponse<List<String>> getPinnedFiles() {
        log.info("Getting pinned files list");

        List<String> pinnedFiles = ipfsServiceEnhanced.getPinnedFiles();
        return ApiResponse.success(pinnedFiles);
    }

    // ==================== 文件查询 ====================

    /**
     * 根据 CID 获取文件信息
     */
    @GetMapping("/file/{cid}")
    public ApiResponse<IpfsFileRecord> getFileByCid(@PathVariable String cid) {
        log.info("Getting file info: {}", cid);

        IpfsFileRecord record = ipfsServiceEnhanced.getFileByCid(cid);
        if (record == null) {
            return ApiResponse.error("文件不存在");
        }

        return ApiResponse.success(record);
    }

    /**
     * 查询文件列表
     */
    @GetMapping("/files")
    public ApiResponse<Page<IpfsFileRecord>> queryFiles(IpfsFileQuery query) {
        log.info("Querying files with filters");

        Page<IpfsFileRecord> page = ipfsServiceEnhanced.queryFiles(query);
        return ApiResponse.success(page);
    }

    /**
     * 获取用户的文件列表
     */
    @GetMapping("/files/user/{userId}")
    public ApiResponse<List<IpfsFileRecord>> getUserFiles(@PathVariable Long userId) {
        log.info("Getting files for user: {}", userId);

        List<IpfsFileRecord> files = ipfsServiceEnhanced.getFilesByUserId(userId);
        return ApiResponse.success(files);
    }

    /**
     * 获取钱包地址的文件列表
     */
    @GetMapping("/files/owner/{ownerAddress}")
    public ApiResponse<List<IpfsFileRecord>> getOwnerFiles(@PathVariable String ownerAddress) {
        log.info("Getting files for owner: {}", ownerAddress);

        List<IpfsFileRecord> files = ipfsServiceEnhanced.getFilesByOwnerAddress(ownerAddress);
        return ApiResponse.success(files);
    }

    // ==================== 文件下载 ====================

    /**
     * 从 IPFS 下载文件
     */
    @GetMapping("/cat/{cid}")
    public ResponseEntity<byte[]> catFile(@PathVariable String cid) {
        log.info("Downloading from IPFS: {}", cid);

        try {
            byte[] data = ipfsServiceEnhanced.downloadFile(cid);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            log.error("Failed to download from IPFS", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取文件内容（根据 CID 自动检测类型）
     */
    @GetMapping("/content/{cid}")
    public ResponseEntity<byte[]> getContent(@PathVariable String cid, HttpServletRequest request) {
        log.info("Getting content from IPFS: {}", cid);

        try {
            // 获取文件记录以确定 MIME 类型
            IpfsFileRecord record = ipfsServiceEnhanced.getFileByCid(cid);
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            if (record != null && record.getMimeType() != null) {
                try {
                    mediaType = MediaType.parseMediaType(record.getMimeType());
                } catch (Exception e) {
                    log.warn("Invalid MIME type: {}", record.getMimeType());
                }
            }

            byte[] data = ipfsServiceEnhanced.downloadFile(cid);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(data);

        } catch (Exception e) {
            log.error("Failed to get content from IPFS", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取 IPFS 网关 URL
     */
    @GetMapping("/gateway/{cid}")
    public ApiResponse<Map<String, Object>> getGatewayUrl(@PathVariable String cid) {
        String primaryUrl = ipfsServiceEnhanced.getGatewayUrl(cid);
        List<String> backupUrls = ipfsServiceEnhanced.getBackupGatewayUrls(cid);

        Map<String, Object> result = new HashMap<>();
        result.put("cid", cid);
        result.put("primaryUrl", primaryUrl);
        result.put("backupUrls", backupUrls);

        return ApiResponse.success(result);
    }

    // ==================== 统计信息 ====================

    /**
     * 获取 IPFS 统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<IpfsStatistics> getStatistics() {
        log.info("Getting IPFS statistics");

        IpfsStatistics statistics = ipfsServiceEnhanced.getStatistics();
        return ApiResponse.success(statistics);
    }

    // ==================== 文件管理 ====================

    /**
     * 更新文件信息
     */
    @PutMapping("/file/{cid}")
    public ApiResponse<Map<String, Object>> updateFileInfo(
            @PathVariable String cid,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags) {

        log.info("Updating file info: {}", cid);

        boolean success = ipfsServiceEnhanced.updateFileInfo(cid, description, tags);

        Map<String, Object> result = new HashMap<>();
        result.put("cid", cid);
        result.put("updated", success);

        if (success) {
            return ApiResponse.success(result, "文件信息更新成功");
        } else {
            return ApiResponse.error("文件信息更新失败");
        }
    }

    /**
     * 删除文件记录
     */
    @DeleteMapping("/file/{cid}")
    public ApiResponse<Map<String, Object>> deleteFile(@PathVariable String cid) {
        log.info("Deleting file record: {}", cid);

        boolean success = ipfsServiceEnhanced.deleteFileRecord(cid);

        Map<String, Object> result = new HashMap<>();
        result.put("cid", cid);
        result.put("deleted", success);

        if (success) {
            return ApiResponse.success(result, "文件记录删除成功");
        } else {
            return ApiResponse.error("文件记录删除失败");
        }
    }
}
