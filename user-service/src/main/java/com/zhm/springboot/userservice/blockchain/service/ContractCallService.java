package com.zhm.springboot.userservice.blockchain.service;

import com.zhm.springboot.userservice.blockchain.config.ContractProperties;
import com.zhm.springboot.userservice.blockchain.contract.NFTAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;

/**
 * 智能合约调用服务
 * 提供合约读取和调用功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractCallService {

    private final ContractProperties contractProperties;
    private final ExecutorService blockchainExecutor;
    private final Web3j web3j;

    @Value("${blockchain.platform-private-key:}")
    private String platformPrivateKey;

    /**
     * 获取 NFT 所有者
     */
    public String getNftOwner(BigInteger tokenId) {
        try {
            NFTAsset contract = NFTAsset.load(
                    contractProperties.getNftAssetAddress(),
                    web3j,
                    getCredentials()
            );
            return contract.ownerOf(tokenId);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to get NFT owner", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.error("Failed to get NFT owner", e);
            return null;
        }
    }

    /**
     * 获取 NFT 元数据 URI
     */
    public String getTokenUri(BigInteger tokenId) {
        try {
            NFTAsset contract = NFTAsset.load(
                    contractProperties.getNftAssetAddress(),
                    web3j,
                    getCredentials()
            );
            return contract.tokenURI(tokenId);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to get token URI", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.error("Failed to get token URI", e);
            return null;
        }
    }

    /**
     * 获取 NFT 当前版本号
     */
    public BigInteger getCurrentVersion(BigInteger tokenId) {
        try {
            NFTAsset contract = NFTAsset.load(
                    contractProperties.getNftAssetAddress(),
                    web3j,
                    getCredentials()
            );
            return contract.getCurrentVersion(tokenId);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to get current version", e);
            Thread.currentThread().interrupt();
            return BigInteger.ZERO;
        } catch (Exception e) {
            log.error("Failed to get current version", e);
            return BigInteger.ZERO;
        }
    }

    /**
     * 获取 NFT 版本历史（从链上读取）
     */
    public List<NFTAsset.TokenVersion> getVersionHistory(BigInteger tokenId) {
        try {
            NFTAsset contract = NFTAsset.load(
                    contractProperties.getNftAssetAddress(),
                    web3j,
                    getCredentials()
            );
            return contract.getVersionHistory(tokenId);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to get version history for tokenId={}", tokenId, e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to get version history for tokenId={}", tokenId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 异步调用合约的 view/pure 方法
     *
     * @param contractAddress 合约地址
     * @param function        Web3j Function 对象
     * @return 调用结果的原始 hex 字符串
     */
    public CompletableFuture<String> callContractAsync(String contractAddress, Function function) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedFunction = FunctionEncoder.encode(function);
                org.web3j.protocol.core.methods.response.EthCall ethCall = web3j.ethCall(
                        org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                                getCredentials().getAddress(),
                                contractAddress,
                                encodedFunction
                        ),
                        DefaultBlockParameterName.LATEST
                ).send();
                if (ethCall.hasError()) {
                    log.error("Contract call error: {}", ethCall.getError().getMessage());
                    return null;
                }
                return ethCall.getValue();
            } catch (Exception e) {
                log.error("Failed to call contract async: contractAddress={}", contractAddress, e);
                return null;
            }
        }, blockchainExecutor);
    }

    /**
     * 获取凭证
     */
    private Credentials getCredentials() {
        return Credentials.create(platformPrivateKey);
    }

    /**
     * 检查合约是否已部署（调用 eth_getCode 验证字节码非空）
     */
    public boolean isContractDeployed(String contractAddress) {
        if (contractAddress == null || contractAddress.isEmpty()) {
            return false;
        }
        try {
            org.web3j.protocol.core.methods.response.EthGetCode ethGetCode = web3j
                    .ethGetCode(contractAddress, DefaultBlockParameterName.LATEST)
                    .send();
            String code = ethGetCode.getCode();
            // 未部署时返回 "0x" 或 "0x0"
            return code != null && code.length() > 2 && !code.equals("0x0");
        } catch (Exception e) {
            log.warn("Failed to check contract deployment for address={}: {}", contractAddress, e.getMessage());
            return false;
        }
    }

    /**
     * 检查 NFT 合约是否已部署
     */
    public boolean isNftContractDeployed() {
        return isContractDeployed(contractProperties.getNftAssetAddress());
    }

    /**
     * 检查市场合约是否已部署
     */
    public boolean isMarketContractDeployed() {
        return isContractDeployed(contractProperties.getMicroMarketAddress());
    }

    /**
     * 检查粉丝代币合约是否已部署
     */
    public boolean isFanTokenContractDeployed() {
        return isContractDeployed(contractProperties.getFanTokenAddress());
    }
}