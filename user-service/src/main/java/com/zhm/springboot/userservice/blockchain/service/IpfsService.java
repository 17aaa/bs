package com.zhm.springboot.userservice.blockchain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhm.springboot.userservice.blockchain.config.IpfsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * IPFS 存储服务
 * 支持 Pinata、本地 IPFS 节点和禁用三种模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpfsService {

    private final IpfsProperties ipfsProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType OCTET_STREAM = MediaType.parse("application/octet-stream");

    /**
     * 上传文件到 IPFS
     * @param file 要上传的文件
     * @return IPFS CID (Content Identifier)
     */
    public String uploadFile(File file) throws IOException {
        if (ipfsProperties.isDisabled()) {
            throw new IOException("IPFS is disabled, cannot upload file");
        }

        if (ipfsProperties.isPinataMode()) {
            return uploadFileToPinata(file);
        }
        return uploadFileToLocal(file);
    }

    /**
     * 上传字节数据到 IPFS
     * @param data 字节数据
     * @param filename 文件名
     * @return IPFS CID
     */
    public String uploadBytes(byte[] data, String filename) throws IOException {
        if (ipfsProperties.isDisabled()) {
            throw new IOException("IPFS is disabled, cannot upload bytes");
        }

        if (ipfsProperties.isPinataMode()) {
            return uploadBytesToPinata(data, filename);
        }
        return uploadBytesToLocal(data, filename);
    }

    /**
     * 上传 JSON 元数据到 IPFS
     * @param metadata 元数据对象
     * @return IPFS CID
     */
    public String uploadMetadata(Object metadata) throws IOException {
        if (ipfsProperties.isDisabled()) {
            throw new IOException("IPFS is disabled, cannot upload metadata");
        }

        if (ipfsProperties.isPinataMode()) {
            return uploadJsonToPinata(metadata);
        }
        return uploadMetadataToLocal(metadata);
    }

    /**
     * 从 IPFS 下载文件
     * @param cid IPFS Content Identifier
     * @return 文件字节数组
     */
    public byte[] downloadFile(String cid) throws IOException {
        String gatewayUrl = getGatewayUrl(cid);
        log.info("Downloading from IPFS gateway: {}", gatewayUrl);

        Request request = new Request.Builder()
                .url(gatewayUrl)
                .get()
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Try backup gateways
                for (String backup : ipfsProperties.getBackupGateways()) {
                    String backupUrl = backup + cid;
                    log.info("Trying backup gateway: {}", backupUrl);
                    Request backupRequest = new Request.Builder()
                            .url(backupUrl)
                            .get()
                            .build();
                    try (Response backupResponse = getClient().newCall(backupRequest).execute()) {
                        if (backupResponse.isSuccessful()) {
                            return backupResponse.body().bytes();
                        }
                    }
                }
                throw new IOException("IPFS download failed from all gateways: " + response);
            }
            return response.body().bytes();
        }
    }

    /**
     * 获取 IPFS 文件的访问 URL
     * @param cid IPFS Content Identifier
     * @return 可访问的网关 URL
     */
    public String getGatewayUrl(String cid) {
        if (cid == null || cid.isEmpty()) {
            return "";
        }
        // If already a full URL, return as-is
        if (cid.startsWith("http://") || cid.startsWith("https://")) {
            return cid;
        }
        // Strip ipfs:// prefix if present
        String cleanCid = cid.startsWith("ipfs://") ? cid.substring(7) : cid;
        return ipfsProperties.getGateway() + cleanCid;
    }

    /**
     * Pin 文件到 IPFS（Pinata 模式下调用 pinByHash）
     * @param cid IPFS CID
     * @return 是否成功
     */
    public boolean pinByHash(String cid) throws IOException {
        if (ipfsProperties.isDisabled()) {
            log.warn("IPFS is disabled, skipping pin for: {}", cid);
            return false;
        }

        if (ipfsProperties.isPinataMode()) {
            return pinByHashPinata(cid);
        }

        // Local mode: use IPFS pin add API
        Request request = new Request.Builder()
                .url(ipfsProperties.getUrl() + "/api/v0/pin/add?arg=" + cid)
                .post(RequestBody.create("", null))
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    /**
     * Unpin 文件（Pinata 模式下调用 unpin）
     * @param cid IPFS CID
     * @return 是否成功
     */
    public boolean unpinByHash(String cid) throws IOException {
        if (ipfsProperties.isDisabled()) {
            log.warn("IPFS is disabled, skipping unpin for: {}", cid);
            return false;
        }

        if (ipfsProperties.isPinataMode()) {
            return unpinByHashPinata(cid);
        }

        // Local mode: use IPFS pin rm API
        Request request = new Request.Builder()
                .url(ipfsProperties.getUrl() + "/api/v0/pin/rm?arg=" + cid)
                .post(RequestBody.create("", null))
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    /**
     * 检查 IPFS 服务是否可用
     */
    public boolean isAvailable() {
        if (ipfsProperties.isDisabled()) {
            return false;
        }

        try {
            if (ipfsProperties.isPinataMode()) {
                // Test Pinata connectivity
                Request request = new Request.Builder()
                        .url(ipfsProperties.getPinataUrl() + "/data/testAuthentication")
                        .get()
                        .header("Authorization", "Bearer " + ipfsProperties.getPinataJwt())
                        .build();
                try (Response response = getClient().newCall(request).execute()) {
                    return response.isSuccessful();
                }
            } else {
                // Test local IPFS node
                Request request = new Request.Builder()
                        .url(ipfsProperties.getUrl() + "/api/v0/id")
                        .post(RequestBody.create("", null))
                        .build();
                try (Response response = getClient().newCall(request).execute()) {
                    return response.isSuccessful();
                }
            }
        } catch (Exception e) {
            log.warn("IPFS service unavailable: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Pinata 模式实现 ====================

    private String uploadFileToPinata(File file) throws IOException {
        log.info("Uploading file to Pinata: {}", file.getName());

        RequestBody fileBody = RequestBody.create(file, OCTET_STREAM);
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(ipfsProperties.getPinataUrl() + "/pinning/pinFileToIPFS")
                .post(multipartBody)
                .header("Authorization", "Bearer " + ipfsProperties.getPinataJwt())
                .build();

        return executePinataUpload(request, "file", file.getName());
    }

    private String uploadBytesToPinata(byte[] data, String filename) throws IOException {
        log.info("Uploading bytes to Pinata: {}", filename);

        RequestBody fileBody = RequestBody.create(data, OCTET_STREAM);
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filename, fileBody)
                .build();

        Request request = new Request.Builder()
                .url(ipfsProperties.getPinataUrl() + "/pinning/pinFileToIPFS")
                .post(multipartBody)
                .header("Authorization", "Bearer " + ipfsProperties.getPinataJwt())
                .build();

        return executePinataUpload(request, "bytes", filename);
    }

    private String uploadJsonToPinata(Object metadata) throws IOException {
        log.info("Uploading metadata JSON to Pinata");

        String json = objectMapper.writeValueAsString(metadata);

        // Pinata JSON pinning uses a different endpoint
        String pinataMetadata = "{}";
        String pinataContent = json;

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("pinataMetadata", pinataMetadata)
                .addFormDataPart("pinataContent", pinataContent)
                .build();

        Request request = new Request.Builder()
                .url(ipfsProperties.getPinataUrl() + "/pinning/pinJSONToIPFS")
                .post(body)
                .header("Authorization", "Bearer " + ipfsProperties.getPinataJwt())
                .build();

        return executePinataUpload(request, "metadata", "metadata.json");
    }

    private String executePinataUpload(Request request, String type, String name) throws IOException {
        int maxRetries = ipfsProperties.getRetryCount();
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Response response = getClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "no body";
                    throw new IOException("Pinata upload failed (attempt " + attempt + "): HTTP " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String cid = jsonNode.get("IpfsHash").asText();

                log.info("Successfully uploaded {} to Pinata: {} -> CID: {}", type, name, cid);
                return cid;
            } catch (IOException e) {
                lastException = e;
                log.warn("Pinata upload attempt {} failed for {}: {}", attempt, name, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Upload interrupted", ie);
                    }
                }
            }
        }

        throw new IOException("Pinata upload failed after " + maxRetries + " attempts: " + name, lastException);
    }

    private boolean pinByHashPinata(String cid) throws IOException {
        log.info("Pinning by hash on Pinata: {}", cid);

        String jsonBody = objectMapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("ipfsPinHash", cid);
        }});

        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(ipfsProperties.getPinataUrl() + "/pinning/pinByHash")
                .post(body)
                .header("Authorization", "Bearer " + ipfsProperties.getPinataJwt())
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    private boolean unpinByHashPinata(String cid) throws IOException {
        log.info("Unpinning on Pinata: {}", cid);

        Request request = new Request.Builder()
                .url(ipfsProperties.getPinataUrl() + "/pinning/unpin/" + cid)
                .delete()
                .header("Authorization", "Bearer " + ipfsProperties.getPinataJwt())
                .build();

        try (Response response = getClient().newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    // ==================== 本地 IPFS 节点模式实现 ====================

    private String uploadFileToLocal(File file) throws IOException {
        log.info("Uploading file to local IPFS node: {}", file.getName());

        RequestBody fileBody = RequestBody.create(file, OCTET_STREAM);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(ipfsProperties.getUrl() + "/api/v0/add")
                .post(requestBody)
                .build();

        return executeLocalUpload(request, file.getName());
    }

    private String uploadBytesToLocal(byte[] data, String filename) throws IOException {
        log.info("Uploading bytes to local IPFS node: {}", filename);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filename,
                        RequestBody.create(data, OCTET_STREAM))
                .build();

        Request request = new Request.Builder()
                .url(ipfsProperties.getUrl() + "/api/v0/add")
                .post(requestBody)
                .build();

        return executeLocalUpload(request, filename);
    }

    private String uploadMetadataToLocal(Object metadata) throws IOException {
        log.info("Uploading metadata to local IPFS node");

        String json = objectMapper.writeValueAsString(metadata);
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(ipfsProperties.getUrl() + "/api/v0/add")
                .post(requestBody)
                .build();

        return executeLocalUpload(request, "metadata.json");
    }

    private String executeLocalUpload(Request request, String name) throws IOException {
        try (Response response = getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Local IPFS upload failed: " + response);
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String cid = jsonNode.get("Hash").asText();

            log.info("Successfully uploaded to local IPFS: {} -> CID: {}", name, cid);
            return cid;
        }
    }

    // ==================== 工具方法 ====================

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(ipfsProperties.getConnectTimeout(), TimeUnit.SECONDS)
                .writeTimeout(ipfsProperties.getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(ipfsProperties.getReadTimeout(), TimeUnit.SECONDS)
                .build();
    }
}