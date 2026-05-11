package com.zhm.springboot.userservice.service;

import com.zhm.springboot.userservice.config.MinIOProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * MinIO 对象存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOService {

    private final MinIOProperties minIOProperties;
    private MinioClient minioClient;

    /**
     * 初始化 MinIO 客户端
     */
    private void initClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(minIOProperties.getEndpoint())
                    .credentials(minIOProperties.getAccessKey(), minIOProperties.getSecretKey())
                    .build();
        }
    }

    /**
     * 确保Bucket存在
     */
    public void ensureBucketExists() {
        if (!minIOProperties.isEnabled()) {
            return;
        }
        try {
            initClient();
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minIOProperties.getBucketName())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minIOProperties.getBucketName())
                                .build()
                );
                log.info("Created MinIO bucket: {}", minIOProperties.getBucketName());
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to ensure bucket exists", e);
        }
    }

    /**
     * 上传文件
     *
     * @param file     文件
     * @param fileType 文件类型（可选，用于生成唯一文件名）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String fileType) {
        if (!minIOProperties.isEnabled()) {
            log.warn("MinIO is not enabled, skipping upload");
            return null;
        }

        try {
            initClient();
            ensureBucketExists();

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = generateObjectName(fileType, extension);

            // 上传文件
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minIOProperties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            // 生成访问URL
            String fileUrl = getFileUrl(objectName);
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件（简化版本，自动检测类型）
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        if (!minIOProperties.isEnabled()) {
            return;
        }

        try {
            initClient();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minIOProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("File deleted successfully: {}", objectName);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to delete file from MinIO", e);
        }
    }

    /**
     * 获取文件信息
     */
    public void getFileInfo(String objectName) {
        if (!minIOProperties.isEnabled()) {
            return;
        }

        try {
            initClient();
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minIOProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to get file info", e);
        }
    }

    /**
     * 生成对象名称
     */
    private String generateObjectName(String fileType, String extension) {
        String prefix = fileType != null ? fileType + "/" : "";
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return prefix + uuid + extension;
    }

    /**
     * 获取文件访问URL
     */
    private String getFileUrl(String objectName) {
        String publicUrl = minIOProperties.getPublicUrl();
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + minIOProperties.getBucketName() + "/" + objectName;
        }
        // 如果没有配置公共URL，使用MinIO服务地址
        return minIOProperties.getEndpoint() + "/" + minIOProperties.getBucketName() + "/" + objectName;
    }

    /**
     * 从URL中提取对象名称
     */
    public String extractObjectName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        String bucketName = minIOProperties.getBucketName();
        String prefix = "/" + bucketName + "/";
        int index = fileUrl.indexOf(prefix);
        if (index >= 0) {
            return fileUrl.substring(index + prefix.length());
        }
        return null;
    }
}