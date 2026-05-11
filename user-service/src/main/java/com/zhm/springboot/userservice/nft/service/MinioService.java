package com.zhm.springboot.userservice.nft.service;

import com.zhm.springboot.userservice.config.MinIOProperties;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储服务
 * 统一管理 NFT 创作时的所有文件类型（图片、PDF、视频等）
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = false)
public class MinioService implements FileStorageService {

    private final MinIOProperties properties;
    private final MinioClient minioClient;

    // 最大文件大小 100MB
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    // 支持的图片类型（用于预览）
    private static final Set<String> IMAGE_TYPES = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "ico"
    );

    @PostConstruct
    public void init() {
        try {
            String bucketName = properties.getBucketName();
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created MinIO bucket: {}", bucketName);
            }

            // 设置 bucket 策略为公开读取
            setBucketPublicPolicy(bucketName);

            log.info("MinIO initialized successfully, bucket: {}, publicUrl: {}",
                    bucketName, properties.getPublicAccessUrl());
        } catch (Exception e) {
            log.error("Failed to initialize MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize MinIO", e);
        }
    }

    /**
     * 设置 Bucket 公开读取策略
     */
    private void setBucketPublicPolicy(String bucketName) throws Exception {
        String policy = """
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]},
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                    }
                ]
            }
            """.formatted(bucketName);

        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policy)
                .build());
    }

    /**
     * 上传文件（统一接口，不区分类型）
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * 上传文件到指定路径
     */
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 100MB");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String objectName = generateObjectName(folder, extension);

            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            String fileUrl = getFileUrl(objectName);

            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("objectName", objectName);
            result.put("filename", originalFilename);
            result.put("size", file.getSize());
            result.put("contentType", contentType);
            result.put("storageType", "minio");
            result.put("isImage", isImageFile(extension));

            log.info("文件上传成功：{} -> {}", originalFilename, objectName);
            return result;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 上传文件（通过输入流）
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());

            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件
     */
    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("获取文件失败: {}", objectName, e);
            throw new RuntimeException("获取文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量删除文件
     */
    public void deleteFiles(List<String> objectNames) {
        for (String objectName : objectNames) {
            try {
                deleteFile(objectName);
            } catch (Exception e) {
                log.warn("删除文件失败: {}", objectName, e);
            }
        }
        log.info("批量删除完成，共 {} 个文件", objectNames.size());
    }

    /**
     * 获取文件访问 URL
     */
    public String getFileUrl(String objectName) {
        String publicUrl = properties.getPublicAccessUrl();
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + properties.getBucketName() + "/" + objectName;
        }

        // 生成预签名 URL（有效期 7 天）
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            log.error("生成文件URL失败: {}", objectName, e);
            throw new RuntimeException("生成文件URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件预签名上传 URL（用于客户端直传）
     */
    public Map<String, Object> getPresignedUploadUrl(String filename, String folder) {
        try {
            String extension = getFileExtension(filename);
            String objectName = generateObjectName(folder, extension);

            String uploadUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .expiry(1, TimeUnit.HOURS)
                    .build());

            Map<String, Object> result = new HashMap<>();
            result.put("uploadUrl", uploadUrl);
            result.put("objectName", objectName);
            result.put("accessUrl", getFileUrl(objectName));
            result.put("expiresIn", 3600);

            return result;
        } catch (Exception e) {
            log.error("获取预签名上传URL失败: {}", filename, e);
            throw new RuntimeException("获取上传URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 列出文件
     */
    public List<Map<String, Object>> listFiles(String prefix) {
        List<Map<String, Object>> fileList = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(properties.getBucketName())
                    .prefix(prefix != null ? prefix : "")
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("objectName", item.objectName());
                fileInfo.put("size", item.size());
                fileInfo.put("lastModified", item.lastModified());
                fileInfo.put("url", getFileUrl(item.objectName()));
                fileInfo.put("isImage", isImageFile(getFileExtension(item.objectName())));
                fileList.add(fileInfo);
            }

            return fileList;
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            throw new RuntimeException("获取文件列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public Map<String, Object> getFileInfo(String objectName) {
        try {
            var stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());

            Map<String, Object> info = new HashMap<>();
            info.put("objectName", objectName);
            info.put("size", stat.size());
            info.put("contentType", stat.contentType());
            info.put("lastModified", stat.lastModified());
            info.put("url", getFileUrl(objectName));
            info.put("etag", stat.etag());

            return info;
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", objectName, e);
            throw new RuntimeException("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 复制文件
     */
    public String copyFile(String sourceObjectName, String targetFolder) {
        try {
            String extension = getFileExtension(sourceObjectName);
            String targetObjectName = generateObjectName(targetFolder, extension);

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(targetObjectName)
                    .source(CopySource.builder()
                            .bucket(properties.getBucketName())
                            .object(sourceObjectName)
                            .build())
                    .build());

            log.info("文件复制成功: {} -> {}", sourceObjectName, targetObjectName);
            return getFileUrl(targetObjectName);
        } catch (Exception e) {
            log.error("文件复制失败: {}", sourceObjectName, e);
            throw new RuntimeException("文件复制失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成对象名称
     */
    private String generateObjectName(String folder, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (folder != null && !folder.isEmpty()) {
            return folder + "/" + timestamp + "_" + uuid + extension;
        }

        // 默认按日期组织
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + timestamp + "_" + uuid + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        return IMAGE_TYPES.contains(ext.toLowerCase());
    }

    /**
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        try {
            minioClient.listBuckets();
            return true;
        } catch (Exception e) {
            log.warn("MinIO service unavailable: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "minio";
    }
}