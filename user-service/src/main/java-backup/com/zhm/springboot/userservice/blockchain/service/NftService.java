package com.zhm.springboot.userservice.blockchain.service;

import com.zhm.springboot.userservice.blockchain.config.ContractProperties;
import com.zhm.springboot.userservice.blockchain.config.PolygonNetworkProperties;
import com.zhm.springboot.userservice.blockchain.contract.NFTAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web3j.protocol.Web3j;
import org.springframework.web3j.tx.gas.ContractGasProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * NFT 服务类
 * 用于调用 NFT Asset 合约
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NftService {

    private final Web3j web3j;
    private final ContractProperties contractProperties;
    private final PolygonNetworkProperties networkProperties;

    private final ContractGasProvider gasProvider = new DefaultGasProvider();

    /**
     * 铸造 NFT
     *
     * @param to            接收地址
     * @param metadataHash  元数据哈希（IPFS CID）
     * @return 代币 ID
     */
    public CompletableFuture<BigInteger> mintNft(String to, String metadataHash) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contractAddress = contractProperties.getNftAssetAddress();
                log.info("Minting NFT to {} with metadata hash {}", to, metadataHash);
                log.info("Using contract address: {}", contractAddress);

                // 加载合约
                NFTAsset nftAsset = NFTAsset.load(
                        contractAddress,
                        web3j,
                        Credentials.create("0x0"), // 占位符，实际需要私钥
                        gasProvider
                );

                // 调用 cast 方法
                // 注意：实际需要有效的 Credentials 才能发送交易
                // 这里仅作为示例
                log.info("NFT mint function prepared for token: {}", to);
                return BigInteger.ZERO; // 占位符返回值

            } catch (Exception e) {
                log.error("Failed to mint NFT: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to mint NFT", e);
            }
        });
    }

    /**
     * 获取 NFT 当前版本
     *
     * @param tokenId 代币 ID
     * @return 版本号
     */
    public CompletableFuture<BigInteger> getCurrentVersion(BigInteger tokenId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contractAddress = contractProperties.getNftAssetAddress();

                NFTAsset nftAsset = NFTAsset.load(
                        contractAddress,
                        web3j,
                        Credentials.create("0x0"),
                        gasProvider
                );

                return nftAsset.getCurrentVersion(tokenId).send();

            } catch (Exception e) {
                log.error("Failed to get current version: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to get current version", e);
            }
        });
    }

    /**
     * 获取 NFT 元数据 URI
     *
     * @param tokenId 代币 ID
     * @return 元数据 URI
     */
    public CompletableFuture<String> getTokenUri(BigInteger tokenId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contractAddress = contractProperties.getNftAssetAddress();

                NFTAsset nftAsset = NFTAsset.load(
                        contractAddress,
                        web3j,
                        Credentials.create("0x0"),
                        gasProvider
                );

                return nftAsset.tokenURI(tokenId).send();

            } catch (Exception e) {
                log.error("Failed to get token URI: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to get token URI", e);
            }
        });
    }

    /**
     * 获取 NFT 所有者
     *
     * @param tokenId 代币 ID
     * @return 所有者地址
     */
    public CompletableFuture<String> getOwnerOf(BigInteger tokenId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contractAddress = contractProperties.getNftAssetAddress();

                NFTAsset nftAsset = NFTAsset.load(
                        contractAddress,
                        web3j,
                        Credentials.create("0x0"),
                        gasProvider
                );

                return nftAsset.ownerOf(tokenId).send();

            } catch (Exception e) {
                log.error("Failed to get owner: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to get owner", e);
            }
        });
    }

    /**
     * 获取余额
     *
     * @param owner 所有者地址
     * @return NFT 数量
     */
    public CompletableFuture<BigInteger> getBalanceOf(String owner) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contractAddress = contractProperties.getNftAssetAddress();

                NFTAsset nftAsset = NFTAsset.load(
                        contractAddress,
                        web3j,
                        Credentials.create("0x0"),
                        gasProvider
                );

                return nftAsset.balanceOf(owner).send();

            } catch (Exception e) {
                log.error("Failed to get balance: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to get balance", e);
            }
        });
    }

    /**
     * 获取版税信息
     *
     * @param tokenId 代币 ID
     * @return 版税信息 [recipient, feeNumerator]
     */
    public CompletableFuture<String[]> getRoyaltyInfo(BigInteger tokenId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contractAddress = contractProperties.getNftAssetAddress();

                NFTAsset nftAsset = NFTAsset.load(
                        contractAddress,
                        web3j,
                        Credentials.create("0x0"),
                        gasProvider
                );

                var result = nftAsset.getRoyaltyInfo(tokenId).send();
                return new String[]{result.component1(), result.component2().toString()};

            } catch (Exception e) {
                log.error("Failed to get royalty info: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to get royalty info", e);
            }
        });
    }

    /**
     * 计算版税金额
     *
     * @param tokenId   代币 ID
     * @param salePrice 销售价格
     * @return 版税金额
     */
    public CompletableFuture<BigInteger> getRoyaltyAmount(BigInteger tokenId, BigInteger salePrice) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contractAddress = contractProperties.getNftAssetAddress();

                NFTAsset nftAsset = NFTAsset.load(
                        contractAddress,
                        web3j,
                        Credentials.create("0x0"),
                        gasProvider
                );

                return nftAsset.royaltyAmount(tokenId, salePrice).send();

            } catch (Exception e) {
                log.error("Failed to get royalty amount: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to get royalty amount", e);
            }
        });
    }
}