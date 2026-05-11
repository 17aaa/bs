package com.zhm.springboot.userservice.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Polygon 网络配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blockchain.network")
public class PolygonNetworkProperties {

    /**
     * 网络类型：amoy(测试网) 或 mainnet(主网)
     */
    private String type = "amoy";

    /**
     * RPC 节点 URL
     */
    private String rpcUrl = "https://rpc-amoy.polygon.technology/";

    /**
     * WebSocket 节点 URL（用于事件监听）
     */
    private String wsUrl = "wss://rpc-amoy.polygon.technology/ws";

    /**
     * 区块链浏览器 URL
     */
    private String explorerUrl = "https://amoy.polygonscan.com";

    /**
     * 链 ID
     */
    private Long chainId = 80002L;

    /**
     * 获取网络类型枚举
     */
    public NetworkType getNetworkType() {
        return NetworkType.fromString(type);
    }

    public enum NetworkType {
        AMOY("amoy", 80002L, "https://rpc-amoy.polygon.technology/", "https://amoy.polygonscan.com"),
        MAINNET("mainnet", 137L, "https://polygon-rpc.com/", "https://polygonscan.com");

        private final String name;
        private final Long chainId;
        private final String rpcUrl;
        private final String explorerUrl;

        NetworkType(String name, Long chainId, String rpcUrl, String explorerUrl) {
            this.name = name;
            this.chainId = chainId;
            this.rpcUrl = rpcUrl;
            this.explorerUrl = explorerUrl;
        }

        public static NetworkType fromString(String text) {
            for (NetworkType networkType : NetworkType.values()) {
                if (networkType.name.equalsIgnoreCase(text)) {
                    return networkType;
                }
            }
            return AMOY; // 默认返回测试网
        }
    }
}