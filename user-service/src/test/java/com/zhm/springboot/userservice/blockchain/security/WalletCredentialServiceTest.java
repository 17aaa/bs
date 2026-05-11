package com.zhm.springboot.userservice.blockchain.security;

import com.zhm.springboot.userservice.blockchain.config.WalletProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 钱包凭证服务测试
 */
class WalletCredentialServiceTest {

    private WalletCredentialService walletCredentialService;

    @Mock
    private WalletProperties walletProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 设置临时目录用于存储 Keystore
        String tempDir = System.getProperty("java.io.tmpdir") + "/test-keystore";
        when(walletProperties.getKeystorePath()).thenReturn(tempDir);
        when(walletProperties.getPlatformPrivateKey()).thenReturn("testPrivateKey");

        walletCredentialService = new WalletCredentialService(walletProperties);
    }

    @Test
    void testGenerateMnemonic() {
        String mnemonic = walletCredentialService.generateMnemonic();
        assertNotNull(mnemonic);
        assertEquals(12, mnemonic.split(" ").length);
    }

    @Test
    void testValidateMnemonic() {
        String mnemonic = walletCredentialService.generateMnemonic();
        assertTrue(walletCredentialService.validateMnemonic(mnemonic));
    }

    @Test
    void testCreateWallet() {
        String password = "testPassword123!@#";
        WalletCredentialService.WalletCredentials credentials =
                walletCredentialService.createWallet(password);

        assertNotNull(credentials);
        assertNotNull(credentials.getAddress());
        assertNotNull(credentials.getMnemonic());
        assertNotNull(credentials.getPrivateKey());
        assertNotNull(credentials.getKeystore());
        assertNotNull(credentials.getKeystorePath());

        // 验证助记词有效
        assertTrue(walletCredentialService.validateMnemonic(credentials.getMnemonic()));
    }

    @Test
    void testRestoreFromMnemonic() {
        // 创建钱包
        WalletCredentialService.WalletCredentials originalCredentials =
                walletCredentialService.createWallet("testPassword");

        // 从助记词恢复
        String restoredPrivateKey = walletCredentialService.restoreFromMnemonic(
                originalCredentials.getMnemonic()
        );

        // 验证私钥不为空
        assertNotNull(restoredPrivateKey);
        assertEquals(64, restoredPrivateKey.length());
    }

    @Test
    void testEncryptAndDecryptPrivateKey() {
        String password = "testPassword123!@#";
        String originalPrivateKey = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234abcd";

        // 加密
        String encrypted = walletCredentialService.encryptPrivateKey(originalPrivateKey, password);
        assertNotNull(encrypted);
        assertNotEquals(originalPrivateKey, encrypted);

        // 解密
        String decrypted = walletCredentialService.decryptPrivateKey(encrypted, password);
        assertEquals(originalPrivateKey, decrypted);
    }

    @Test
    void testLoadFromPrivateKey() {
        String privateKey = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234abcd";
        String credentials = walletCredentialService.loadFromPrivateKey(privateKey);

        assertNotNull(credentials);
        assertEquals(privateKey, credentials);
    }

    @Test
    void testValidateInvalidMnemonic() {
        // 测试无效助记词
        assertFalse(walletCredentialService.validateMnemonic(""));
        assertFalse(walletCredentialService.validateMnemonic("invalid word"));
        assertFalse(walletCredentialService.validateMnemonic("word1 word2 word3"));
    }
}