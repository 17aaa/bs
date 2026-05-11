package com.zhm.springboot.userservice.blockchain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhm.springboot.userservice.blockchain.config.IpfsProperties;
import com.zhm.springboot.userservice.blockchain.dto.IpfsUploadResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NFT 元数据服务
 * 处理 NFT 元数据的创建和上传到 IPFS
 * 元数据中的 image 字段使用 ipfs:// URI 格式（符合 ERC-721 标准）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NftMetadataService {

    private final IpfsServiceEnhanced ipfsService;
    private final IpfsProperties ipfsProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建并上传 NFT 元数据（包含图片）
     * 图片上传到 IPFS，元数据 JSON 也上传到 IPFS
     */
    public NftMetadataResult createNftMetadata(String name, String description,
                                                MultipartFile image,
                                                List<MetadataAttribute> attributes,
                                                Long userId) throws IOException {
        log.info("Creating NFT meta {}", name);

        // 1. 上传图片到 IPFS（IpfsServiceEnhanced 内部同时存 MinIO + IPFS）
        String imageCid = ipfsService.uploadBytes(image.getBytes(), image.getOriginalFilename());
        String imageUrl = ipfsService.getGatewayUrl(imageCid);

        log.info("Image uploaded, CID: {}, Gateway URL: {}", imageCid, imageUrl);

        // 2. 构建符合 ERC-721 标准的元数据（image 字段使用 ipfs:// URI）
        Map<String, Object> metadata = buildMetadata(name, description, imageCid, attributes);

        // 3. 上传元数据 JSON 到 IPFS
        String metadataCid = ipfsService.uploadMetadata(metadata);
        String metadataUrl = "ipfs://" + metadataCid;
        String metadataGatewayUrl = ipfsService.getGatewayUrl(metadataCid);

        log.info("NFT metadata created. Metadata CID: {}, Token URI: {}", metadataCid, metadataUrl);

        return NftMetadataResult.builder()
                .name(name)
                .description(description)
                .imageCid(imageCid)
                .imageUrl(imageUrl)
                .metadataCid(metadataCid)
                .metadataUrl(metadataUrl)
                .metadataGatewayUrl(metadataGatewayUrl)
                .attributes(attributes)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 创建并上传 NFT 元数据（图片已上传）
     * imageCid 应为真实 IPFS CID
     */
    public NftMetadataResult createNftMetadataWithExistingImage(String name, String description,
                                                                  String imageCid,
                                                                  List<MetadataAttribute> attributes) throws IOException {
        log.info("Creating NFT metadata with existing image CID: {}", imageCid);

        String imageUrl = ipfsService.getGatewayUrl(imageCid);

        // 构建元数据
        Map<String, Object> metadata = buildMetadata(name, description, imageCid, attributes);

        // 上传元数据到 IPFS
        String metadataCid = ipfsService.uploadMetadata(metadata);
        String metadataUrl = "ipfs://" + metadataCid;
        String metadataGatewayUrl = ipfsService.getGatewayUrl(metadataCid);

        log.info("NFT metadata created. Metadata CID: {}, Token URI: {}", metadataCid, metadataUrl);

        return NftMetadataResult.builder()
                .name(name)
                .description(description)
                .imageCid(imageCid)
                .imageUrl(imageUrl)
                .metadataCid(metadataCid)
                .metadataUrl(metadataUrl)
                .metadataGatewayUrl(metadataGatewayUrl)
                .attributes(attributes)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 批量创建 NFT 元数据
     */
    public List<NftMetadataResult> batchCreateNftMetadata(List<NftMetadataInput> inputs, Long userId) {
        List<NftMetadataResult> results = new ArrayList<>();

        for (NftMetadataInput input : inputs) {
            try {
                NftMetadataResult result = createNftMetadata(
                        input.getName(),
                        input.getDescription(),
                        input.getImage(),
                        input.getAttributes(),
                        userId
                );
                results.add(result);
            } catch (IOException e) {
                log.error("Failed to create metadata for: {}", input.getName(), e);
            }
        }

        return results;
    }

    /**
     * 更新 NFT 元数据（创建新版本）
     */
    public NftMetadataResult updateNftMetadata(String originalCid,
                                                  String name,
                                                  String description,
                                                  String imageCid,
                                                  List<MetadataAttribute> attributes,
                                                  Integer version) throws IOException {
        log.info("Updating NFT metadata. Original CID: {}, Version: {}", originalCid, version);

        // 构建新版本元数据
        Map<String, Object> metadata = buildMetadata(name, description, imageCid, attributes);

        // 添加版本信息
        metadata.put("version", version != null ? version : 1);
        metadata.put("previousVersion", originalCid);
        metadata.put("updatedAt", LocalDateTime.now().toString());

        // 上传新版本到 IPFS
        String newMetadataCid = ipfsService.uploadMetadata(metadata);
        String newMetadataUrl = "ipfs://" + newMetadataCid;
        String newMetadataGatewayUrl = ipfsService.getGatewayUrl(newMetadataCid);

        String imageUrl = ipfsService.getGatewayUrl(imageCid);

        return NftMetadataResult.builder()
                .name(name)
                .description(description)
                .imageCid(imageCid)
                .imageUrl(imageUrl)
                .metadataCid(newMetadataCid)
                .metadataUrl(newMetadataUrl)
                .metadataGatewayUrl(newMetadataGatewayUrl)
                .attributes(attributes)
                .previousVersion(originalCid)
                .version(version)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 验证元数据格式
     */
    public boolean validateMetadata(String metadataJson) {
        try {
            Map<String, Object> metadata = objectMapper.readValue(metadataJson, Map.class);

            if (!metadata.containsKey("name") || !(metadata.get("name") instanceof String)) {
                return false;
            }

            // image 字段应为 ipfs:// URI 或 HTTP URL
            if (metadata.containsKey("image")) {
                Object image = metadata.get("image");
                if (!(image instanceof String)) {
                    return false;
                }
                String imageStr = (String) image;
                if (!imageStr.startsWith("ipfs://") && !imageStr.startsWith("http")) {
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 解析元数据 JSON
     */
    public Map<String, Object> parseMetadata(String metadataJson) throws IOException {
        return objectMapper.readValue(metadataJson, Map.class);
    }

    /**
     * 构建标准 NFT 元数据（ERC-721 规范）
     * image 字段使用 ipfs://CID 格式，这是 NFT 标准
     */
    private Map<String, Object> buildMetadata(String name, String description,
                                               String imageCid,
                                               List<MetadataAttribute> attributes) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("name", name);
        metadata.put("description", description);

        // image 字段使用 ipfs://CID 格式（ERC-721 标准）
        // 如果 imageCid 已经是完整 URL（MinIO 场景），直接使用
        if (imageCid.startsWith("http://") || imageCid.startsWith("https://")) {
            metadata.put("image", imageCid);
        } else {
            // 真实 IPFS CID，使用 ipfs:// URI
            String cleanCid = imageCid.startsWith("ipfs://") ? imageCid.substring(7) : imageCid;
            metadata.put("image", "ipfs://" + cleanCid);
        }

        if (attributes != null && !attributes.isEmpty()) {
            List<Map<String, Object>> attrs = new ArrayList<>();
            for (MetadataAttribute attr : attributes) {
                Map<String, Object> attrMap = new HashMap<>();
                attrMap.put("trait_type", attr.getTraitType());
                attrMap.put("value", attr.getValue());
                if (attr.getDisplayType() != null) {
                    attrMap.put("display_type", attr.getDisplayType());
                }
                attrs.add(attrMap);
            }
            metadata.put("attributes", attrs);
        }

        metadata.put("created_at", LocalDateTime.now().toString());

        return metadata;
    }

    // ==================== DTO 类 ====================

    @Data
    @Builder
    public static class NftMetadataResult {
        private String name;
        private String description;
        private String imageCid;
        private String imageUrl;
        private String metadataCid;
        /** Token URI，格式为 ipfs://CID，存储到链上 */
        private String metadataUrl;
        /** 元数据网关 URL，用于 HTTP 访问 */
        private String metadataGatewayUrl;
        private List<MetadataAttribute> attributes;
        private String previousVersion;
        private Integer version;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class NftMetadataInput {
        private String name;
        private String description;
        private MultipartFile image;
        private List<MetadataAttribute> attributes;
    }

    @Data
    @Builder
    public static class MetadataAttribute {
        private String traitType;
        private String value;
        private String displayType;

        public static MetadataAttribute of(String traitType, String value) {
            return MetadataAttribute.builder()
                    .traitType(traitType)
                    .value(value)
                    .build();
        }

        public static MetadataAttribute of(String traitType, String value, String displayType) {
            return MetadataAttribute.builder()
                    .traitType(traitType)
                    .value(value)
                    .displayType(displayType)
                    .build();
        }
    }
}