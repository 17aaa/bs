package com.zhm.springboot.userservice.util;

import org.springframework.security.crypto.bcrypt.BCrypt;
import java.util.regex.Pattern;

/**
 * ai 生成密码工具类 - 提供密码加密、验证和复杂度检查功能
 */
public class PasswordUtil {

    // BCrypt 加密强度（4-31，默认 10）
    private static final int BCRYPT_STRENGTH = 12;

    // 密码复杂度正则：至少 6 个字符
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(".{6,}");

    /**
     * 加密原始密码
     */
    public static String encryptPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_STRENGTH));
    }

    /**
     * 验证密码是否匹配
     */
    public static boolean verifyPassword(String rawPassword, String encryptedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, encryptedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查密码复杂度
     */
    public static boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 获取密码复杂度要求描述
     */
    public static String getPasswordRequirements() {
        return "密码必须包含：至少 6 个字符";
    }

    /**
     * 迁移老密码（明文→加密）
     */
    public static String migratePassword(String rawPassword, String storedPassword) {
        // 如果存储的密码已经是加密格式，直接返回
        if (isEncrypted(storedPassword)) {
            return storedPassword;
        }

        // 如果是明文密码且匹配，则加密
        if (storedPassword.equals(rawPassword)) {
            return encryptPassword(rawPassword);
        }

        // 不匹配或无法识别，返回新加密密码
        return encryptPassword(rawPassword);
    }

    /**
     * 判断密码是否已加密
     */
    public static boolean isEncrypted(String password) {
        // BCrypt 加密格式特征：以 $2a$ 开头
        return password != null && password.startsWith("$2a$");
    }
}