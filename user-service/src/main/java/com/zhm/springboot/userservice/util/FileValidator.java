package com.zhm.springboot.userservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传安全验证工具
 */
@Slf4j
public class FileValidator {

    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // 允许的文档类型
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf"
    );

    // 允许的压缩文件类型
    private static final List<String> ALLOWED_ARCHIVE_TYPES = Arrays.asList(
            "application/zip", "application/x-zip-compressed",
            "application/x-rar-compressed", "application/x-tar",
            "application/gzip", "application/x-gzip"
    );

    // 文件扩展名白名单
    private static final Map<String, List<String>> EXTENSION_WHITELIST = new HashMap<>() {{
        put("image", Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
        put("document", Arrays.asList(".pdf"));
        put("archive", Arrays.asList(".zip", ".rar", ".tar", ".gz", ".7z"));
    }};

    // 文件大小限制（字节）
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;   // 50MB

    // 危险文件扩展名
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            ".exe", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".jar",
            ".php", ".jsp", ".asp", ".aspx", ".html", ".htm"
    );

    /**
     * 验证图片文件
     */
    public static ValidationResult validateImage(MultipartFile file) {
        return validate(file, "image", MAX_IMAGE_SIZE);
    }

    /**
     * 验证文档文件
     */
    public static ValidationResult validateDocument(MultipartFile file) {
        return validate(file, "document", MAX_FILE_SIZE);
    }

    /**
     * 验证压缩文件
     */
    public static ValidationResult validateArchive(MultipartFile file) {
        return validate(file, "archive", MAX_FILE_SIZE);
    }

    /**
     * 通用验证方法
     */
    public static ValidationResult validate(MultipartFile file, String type, long maxSize) {
        ValidationResult result = new ValidationResult();

        if (file == null || file.isEmpty()) {
            result.setValid(false);
            result.setError("文件为空");
            return result;
        }

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();

        // 1. 检查文件名
        if (filename == null || filename.isEmpty()) {
            result.setValid(false);
            result.setError("文件名无效");
            return result;
        }

        // 2. 检查危险扩展名
        String extension = getFileExtension(filename).toLowerCase();
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            log.warn("检测到危险文件类型: {}", filename);
            result.setValid(false);
            result.setError("不允许上传此类型的文件");
            return result;
        }

        // 3. 检查扩展名白名单
        List<String> allowedExtensions = EXTENSION_WHITELIST.get(type);
        if (allowedExtensions != null && !allowedExtensions.contains(extension)) {
            result.setValid(false);
            result.setError("文件扩展名不支持，允许: " + String.join(", ", allowedExtensions));
            return result;
        }

        // 4. 检查Content-Type
        List<String> allowedTypes = getAllowedTypes(type);
        if (contentType != null && !allowedTypes.contains(contentType)) {
            log.warn("Content-Type不匹配: {} for type {}", contentType, type);
            // 不直接拒绝，因为有些浏览器可能发送不正确的Content-Type
        }

        // 5. 检查文件大小
        if (size > maxSize) {
            result.setValid(false);
            result.setError("文件大小超过限制: " + formatSize(maxSize));
            return result;
        }

        // 6. 检查文件内容（魔数验证）
        try {
            if (!validateFileContent(file, type)) {
                result.setValid(false);
                result.setError("文件内容与扩展名不匹配");
                return result;
            }
        } catch (IOException e) {
            log.warn("文件内容验证失败: {}", e.getMessage());
            // 内容验证失败不阻止上传，只记录日志
        }

        result.setValid(true);
        return result;
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot).toLowerCase();
        }
        return "";
    }

    /**
     * 获取允许的Content-Type列表
     */
    private static List<String> getAllowedTypes(String type) {
        switch (type) {
            case "image":
                return ALLOWED_IMAGE_TYPES;
            case "document":
                return ALLOWED_DOCUMENT_TYPES;
            case "archive":
                return ALLOWED_ARCHIVE_TYPES;
            default:
                return ALLOWED_IMAGE_TYPES;
        }
    }

    /**
     * 验证文件内容（魔数验证）
     */
    private static boolean validateFileContent(MultipartFile file, String type) throws IOException {
        byte[] bytes = file.getBytes();
        if (bytes.length < 4) return true;

        // 图片魔数验证
        if ("image".equals(type)) {
            String hex = bytesToHex(bytes[0], bytes[1], bytes[2], bytes[3]);
            // JPEG: FFD8FF
            if (hex.startsWith("FFD8FF")) return true;
            // PNG: 89504E47
            if (hex.startsWith("89504E47")) return true;
            // GIF: 47494638
            if (hex.startsWith("47494638")) return true;
            // WebP: 52494646...57454250
            if (hex.startsWith("52494646") && bytes.length > 11) {
                String webpHex = bytesToHex(bytes[8], bytes[9], bytes[10], bytes[11]);
                if (webpHex.startsWith("57454250")) return true;
            }
            return false;
        }

        // PDF魔数验证
        if ("document".equals(type)) {
            String hex = bytesToHex(bytes[0], bytes[1], bytes[2], bytes[3]);
            // PDF: 25504446
            return hex.startsWith("25504446");
        }

        // ZIP类文件验证
        if ("archive".equals(type)) {
            String hex = bytesToHex(bytes[0], bytes[1]);
            // ZIP: 504B
            if (hex.equals("504B")) return true;
            // RAR: 52617221
            String hex4 = bytesToHex(bytes[0], bytes[1], bytes[2], bytes[3]);
            if (hex4.startsWith("52617221")) return true;
        }

        return true;
    }

    /**
     * 字节转十六进制字符串
     */
    private static String bytesToHex(byte... bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 格式化文件大小
     */
    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / 1024 / 1024) + " MB";
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String error;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}