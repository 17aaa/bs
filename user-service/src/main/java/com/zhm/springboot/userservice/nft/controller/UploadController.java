package com.zhm.springboot.userservice.nft.controller;

import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.config.MinIOProperties;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 文件上传控制器
 * 使用 MinIO 对象存储统一管理所有文件类型
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UploadController {

    private final MinIOProperties minioProperties;
    private MinioClient minioClient;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    @PostConstruct
    public void init() {
        if (!minioProperties.isEnabled()) {
            log.info("MinIO is disabled");
            return;
        }

        try {
            minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            String bucketName = minioProperties.getBucketName();
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created MinIO bucket: {}", bucketName);
            }

            // 设置公开读取策略
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

            log.info("MinIO initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO: {}", e.getMessage(), e);
        }
    }

    /**
     * 上传文件（统一接口）
     */
    @PostMapping("/file")
    public ApiResponse<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ApiResponse.error("文件大小不能超过 100MB");
        }
        if (!minioProperties.isEnabled() || minioClient == null) {
            return ApiResponse.error("存储服务未启用");
        }

        try {
            Map<String, Object> result = doUpload(file, null);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传 NFT 资产文件
     */
    @PostMapping("/nft/asset")
    public ApiResponse<Map<String, Object>> uploadNftAsset(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ApiResponse.error("文件大小不能超过 100MB");
        }
        if (!minioProperties.isEnabled() || minioClient == null) {
            return ApiResponse.error("存储服务未启用");
        }

        try {
            Map<String, Object> result = doUpload(file, "uploads");
            result.put("type", "nft-asset");
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("NFT资产上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 执行上传
     */
    private Map<String, Object> doUpload(MultipartFile file, String folder) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String objectName = generateObjectName(folder, extension);
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
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

        log.info("文件上传成功：{} -> {}", originalFilename, objectName);
        return result;
    }

    /**
     * 获取文件
     */
    @GetMapping("/file")
    public ResponseEntity<byte[]> getFile(@RequestParam String objectName) {
        if (!minioProperties.isEnabled() || minioClient == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            String contentType = getContentType(objectName);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (Exception e) {
            log.error("获取文件失败: {}", objectName, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/file")
    public ApiResponse<Void> deleteFile(@RequestParam String objectName) {
        if (!minioProperties.isEnabled() || minioClient == null) {
            return ApiResponse.error("存储服务未启用");
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 检查存储服务状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("minioEnabled", minioProperties.isEnabled());
        status.put("minioAvailable", minioClient != null);
        status.put("bucket", minioProperties.getBucketName());
        status.put("maxFileSize", MAX_FILE_SIZE);
        return ApiResponse.success(status);
    }

    /**
     * 获取文件URL
     * 返回前端代理路径，让前端可以通过代理访问MinIO
     */
    private String getFileUrl(String objectName) {
        // 返回前端代理路径，格式：/minio/bucket/objectName
        return "/minio/" + minioProperties.getBucketName() + "/" + objectName;
    }

    private String generateObjectName(String folder, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (folder != null && !folder.isEmpty()) {
            return folder + "/" + timestamp + "_" + uuid + extension;
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + timestamp + "_" + uuid + extension;
    }

    private String getContentType(String filename) {
        String ext = filename.toLowerCase();

        // 图片
        if (ext.endsWith(".jpg") || ext.endsWith(".jpeg")) return "image/jpeg";
        if (ext.endsWith(".png")) return "image/png";
        if (ext.endsWith(".gif")) return "image/gif";
        if (ext.endsWith(".webp")) return "image/webp";
        if (ext.endsWith(".svg")) return "image/svg+xml";
        if (ext.endsWith(".bmp")) return "image/bmp";
        if (ext.endsWith(".ico")) return "image/x-icon";
        if (ext.endsWith(".tiff") || ext.endsWith(".tif")) return "image/tiff";
        if (ext.endsWith(".heic") || ext.endsWith(".heif")) return "image/heic";

        // 视频
        if (ext.endsWith(".mp4")) return "video/mp4";
        if (ext.endsWith(".webm")) return "video/webm";
        if (ext.endsWith(".ogg")) return "video/ogg";
        if (ext.endsWith(".mov")) return "video/quicktime";
        if (ext.endsWith(".avi")) return "video/x-msvideo";
        if (ext.endsWith(".mkv")) return "video/x-matroska";
        if (ext.endsWith(".wmv")) return "video/x-ms-wmv";
        if (ext.endsWith(".flv")) return "video/x-flv";

        // 音频
        if (ext.endsWith(".mp3")) return "audio/mpeg";
        if (ext.endsWith(".wav")) return "audio/wav";
        if (ext.endsWith(".flac")) return "audio/flac";
        if (ext.endsWith(".aac")) return "audio/aac";
        if (ext.endsWith(".m4a")) return "audio/mp4";
        if (ext.endsWith(".ogg")) return "audio/ogg";
        if (ext.endsWith(".wma")) return "audio/x-ms-wma";

        // PDF
        if (ext.endsWith(".pdf")) return "application/pdf";

        // Word文档
        if (ext.endsWith(".doc")) return "application/msword";
        if (ext.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (ext.endsWith(".rtf")) return "application/rtf";

        // Excel表格
        if (ext.endsWith(".xls")) return "application/vnd.ms-excel";
        if (ext.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (ext.endsWith(".csv")) return "text/csv";

        // PPT演示
        if (ext.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (ext.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

        // 压缩文件
        if (ext.endsWith(".zip")) return "application/zip";
        if (ext.endsWith(".rar")) return "application/vnd.rar";
        if (ext.endsWith(".7z")) return "application/x-7z-compressed";
        if (ext.endsWith(".tar")) return "application/x-tar";
        if (ext.endsWith(".gz")) return "application/gzip";

        // 3D模型
        if (ext.endsWith(".glb")) return "model/gltf-binary";
        if (ext.endsWith(".gltf")) return "model/gltf+json";
        if (ext.endsWith(".obj")) return "model/obj";
        if (ext.endsWith(".stl")) return "model/stl";
        if (ext.endsWith(".fbx")) return "model/fbx";

        // 代码和文本
        if (ext.endsWith(".json")) return "application/json";
        if (ext.endsWith(".xml")) return "application/xml";
        if (ext.endsWith(".html")) return "text/html";
        if (ext.endsWith(".css")) return "text/css";
        if (ext.endsWith(".js")) return "application/javascript";
        if (ext.endsWith(".ts")) return "application/typescript";
        if (ext.endsWith(".py")) return "text/x-python";
        if (ext.endsWith(".java")) return "text/x-java-source";
        if (ext.endsWith(".md")) return "text/markdown";
        if (ext.endsWith(".txt")) return "text/plain";

        return "application/octet-stream";
    }
}