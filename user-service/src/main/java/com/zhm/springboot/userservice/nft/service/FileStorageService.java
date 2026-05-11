package com.zhm.springboot.userservice.nft.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件存储服务接口
 * 统一 MinIO 和本地存储的接口
 */
public interface FileStorageService {

    /**
     * 上传文件
     */
    Map<String, Object> uploadFile(MultipartFile file);

    /**
     * 上传文件到指定文件夹
     */
    Map<String, Object> uploadFile(MultipartFile file, String folder);

    /**
     * 获取文件流
     */
    InputStream getFile(String objectName);

    /**
     * 删除文件
     */
    void deleteFile(String objectName);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String objectName);

    /**
     * 检查文件是否存在
     */
    boolean fileExists(String objectName);

    /**
     * 列出文件
     */
    List<Map<String, Object>> listFiles(String prefix);

    /**
     * 检查服务是否可用
     */
    boolean isAvailable();

    /**
     * 获取存储类型
     */
    default String getStorageType() {
        return "unknown";
    }
}