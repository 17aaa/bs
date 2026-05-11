package com.zhm.springboot.userservice.nft.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 本地文件存储服务
 * 当 MinIO 未启用时使用
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalStorageService implements FileStorageService {

    @Value("${storage.local.path:${user.dir}/uploads}")
    private String uploadPath;

    @Value("${storage.local.baseUrl:http://localhost:8085/api/upload}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(uploadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            log.info("Local storage initialized, path: {}", uploadPath);
        } catch (IOException e) {
            log.error("Failed to initialize local storage", e);
            throw new RuntimeException("Failed to initialize local storage", e);
        }
    }

    /**
     * 上传文件
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * 上传文件到指定文件夹
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
            String filename = generateFilename(extension);
            String relativePath = folder != null ? folder + "/" + filename : filename;

            // 创建目录
            Path targetDir = Paths.get(uploadPath, folder != null ? folder : "");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 保存文件
            Path targetPath = Paths.get(uploadPath, relativePath);
            Files.copy(file.getInputStream(), targetPath);

            String fileUrl = baseUrl + "/" + relativePath.replace("\\", "/");

            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("objectName", relativePath.replace("\\", "/"));
            result.put("filename", originalFilename);
            result.put("size", file.getSize());
            result.put("contentType", file.getContentType());
            result.put("storageType", "local");

            log.info("文件上传成功（本地存储）：{} -> {}", originalFilename, relativePath);
            return result;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取文件
     */
    public InputStream getFile(String objectName) {
        try {
            Path filePath = Paths.get(uploadPath, objectName);
            return new FileInputStream(filePath.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("文件不存在: " + objectName, e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            Path filePath = Paths.get(uploadPath, objectName);
            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", objectName);
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件URL
     */
    public String getFileUrl(String objectName) {
        return baseUrl + "/" + objectName.replace("\\", "/");
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String objectName) {
        return Files.exists(Paths.get(uploadPath, objectName));
    }

    /**
     * 列出文件
     */
    public List<Map<String, Object>> listFiles(String prefix) {
        List<Map<String, Object>> fileList = new ArrayList<>();
        try {
            Path searchPath = prefix != null ? Paths.get(uploadPath, prefix) : Paths.get(uploadPath);
            if (!Files.exists(searchPath)) {
                return fileList;
            }

            Files.walk(searchPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        Path relativePath = Paths.get(uploadPath).relativize(path);
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("objectName", relativePath.toString().replace("\\", "/"));
                        try {
                            fileInfo.put("size", Files.size(path));
                            fileInfo.put("lastModified", Files.getLastModifiedTime(path).toMillis());
                        } catch (IOException ignored) {}
                        fileInfo.put("url", getFileUrl(relativePath.toString()));
                        fileList.add(fileInfo);
                    });

            return fileList;
        } catch (IOException e) {
            log.error("获取文件列表失败", e);
            return fileList;
        }
    }

    /**
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        return Files.exists(Paths.get(uploadPath));
    }

    @Override
    public String getStorageType() {
        return "local";
    }

    private String generateFilename(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + timestamp + "_" + uuid + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}