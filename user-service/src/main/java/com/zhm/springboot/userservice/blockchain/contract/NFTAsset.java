package com.zhm.springboot.userservice.blockchain.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint96;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * NFTAsset 智能合约调用工具类
 * 用于 Web3j 调用
 */
public class NFTAsset {

    /** 对应 Solidity struct TokenVersion */
    @Data
    @AllArgsConstructor
    public static class TokenVersion {
        private BigInteger version;
        private String metadataHash;
        private String ipfsUri;
        private BigInteger timestamp;
        private String updater;
    }

    @Getter
    private final String contractAddress;
    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;

    public NFTAsset(String contractAddress, Web3j web3j, Credentials credentials) {
        this(contractAddress, web3j, credentials, new DefaultGasProvider());
    }

    public NFTAsset(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        this.contractAddress = contractAddress;
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /**
     * 铸造 NFT
     */
    public TransactionReceipt mint(String to, String metadataHash) throws Exception {
        final Function function = new Function(
                "mint",
                Arrays.asList(new Address(to), new Utf8String(metadataHash)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 更新元数据
     */
    public TransactionReceipt updateMetadata(BigInteger tokenId, String newMetadataHash) throws Exception {
        final Function function = new Function(
                "updateMetadata",
                Arrays.asList(new Uint256(tokenId), new Utf8String(newMetadataHash)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 获取当前版本号
     */
    public BigInteger getCurrentVersion(BigInteger tokenId) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "getCurrentVersion",
                Arrays.asList(new Uint256(tokenId)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 获取 NFT 版本历史
     * 对应合约：getVersionHistory(uint256 tokenId) returns (TokenVersion[] memory)
     * TokenVersion: (uint256 version, string metadataHash, string ipfsUri, uint256 timestamp, address updater)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<TokenVersion> getVersionHistory(BigInteger tokenId) throws ExecutionException, InterruptedException {
        // 构造只有输入参数的 Function，手动调用并解析 rawHex
        String encodedFunction = FunctionEncoder.encode(new Function(
                "getVersionHistory",
                Arrays.asList(new Uint256(tokenId)),
                Collections.emptyList()
        ));

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            credentials.getAddress(),
                            contractAddress,
                            encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();
        } catch (IOException e) {
            throw new ExecutionException("Failed to call getVersionHistory", e);
        }

        String rawHex = ethCall.getValue();
        if (rawHex == null || rawHex.equals("0x") || rawHex.length() <= 2) {
            return Collections.emptyList();
        }

        // 使用 raw TypeReference 列表避免 Java 泛型上限冲突
        List outputParams = Arrays.asList(
                TypeReference.create(DynamicArray.class)
        );

        List<Type> decoded = FunctionReturnDecoder.decode(rawHex, outputParams);
        List<TokenVersion> result = new ArrayList<>();

        if (!decoded.isEmpty() && decoded.get(0) instanceof DynamicArray) {
            DynamicArray<?> array = (DynamicArray<?>) decoded.get(0);
            for (Object item : array.getValue()) {
                if (item instanceof DynamicStruct) {
                    DynamicStruct struct = (DynamicStruct) item;
                    List<Type> fields = struct.getValue();
                    if (fields.size() >= 5) {
                        result.add(new TokenVersion(
                                (BigInteger) fields.get(0).getValue(),
                                (String) fields.get(1).getValue(),
                                (String) fields.get(2).getValue(),
                                (BigInteger) fields.get(3).getValue(),
                                (String) fields.get(4).getValue()
                        ));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 设置版税
     */
    public TransactionReceipt setRoyalty(BigInteger tokenId, String recipient, BigInteger feeNumerator) throws Exception {
        final Function function = new Function(
                "setRoyalty",
                Arrays.asList(new Uint256(tokenId), new Address(recipient), new Uint96(feeNumerator)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 获取版税信息
     */
    public Tuple2<String, BigInteger> getRoyaltyInfo(BigInteger tokenId) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "getRoyaltyInfo",
                Arrays.asList(new Uint256(tokenId)),
                Arrays.asList(
                        new TypeReference<Address>() {},
                        new TypeReference<Uint96>() {}
                )
        );
        List<Type> results = FunctionReturnDecoder.decode(
                executeCallRaw(function),
                function.getOutputParameters()
        );
        return new Tuple2<>((String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    /**
     * 版税金额
     */
    public BigInteger royaltyAmount(BigInteger tokenId, BigInteger salePrice) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "royaltyAmount",
                Arrays.asList(new Uint256(tokenId), new Uint256(salePrice)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 获取 Token URI
     */
    public String tokenURI(BigInteger tokenId) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "tokenURI",
                Arrays.asList(new Uint256(tokenId)),
                Collections.singletonList(new TypeReference<Utf8String>() {})
        );
        return executeCall(function);
    }

    /**
     * 获取所有者
     */
    public String ownerOf(BigInteger tokenId) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "ownerOf",
                Arrays.asList(new Uint256(tokenId)),
                Collections.singletonList(new TypeReference<Address>() {})
        );
        return executeCall(function);
    }

    /**
     * 余额查询
     */
    public BigInteger balanceOf(String owner) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "balanceOf",
                Arrays.asList(new Address(owner)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 执行交易（写入操作）
     */
    private TransactionReceipt executeTransaction(Function function) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        // 使用 RawTransactionManager 发送交易
        RawTransactionManager rawTransactionManager = new RawTransactionManager(
                web3j,
                credentials,
                80002L  // Polygon Amoy chain ID
        );
        EthSendTransaction ethSendTransaction = rawTransactionManager.sendTransaction(
                BigInteger.valueOf(30_000_000_000L), // gasPrice
                BigInteger.valueOf(500_000L),        // gasLimit
                contractAddress,
                encodedFunction,
                BigInteger.ZERO,                      // value
                false                                 // shouldCheckNonce
        );

        // 检查是否有错误
        if (ethSendTransaction.hasError()) {
            throw new RuntimeException("Transaction failed: " + ethSendTransaction.getError().getMessage());
        }

        // 获取交易哈希
        String transactionHash = ethSendTransaction.getTransactionHash();
        if (transactionHash == null || transactionHash.isEmpty()) {
            throw new RuntimeException("Failed to get transaction hash");
        }

        // 等待交易回执
        return web3j.ethGetTransactionReceipt(transactionHash).send().getResult();
    }

    /**
     * 执行调用（读取操作）
     */
    @SuppressWarnings("unchecked")
    private <T> T executeCall(Function function) throws ExecutionException, InterruptedException {
        String encodedFunction = FunctionEncoder.encode(function);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            credentials.getAddress(),
                            contractAddress,
                            encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();
        } catch (IOException e) {
            throw new ExecutionException("Failed to call contract", e);
        }

        List<Type> results = FunctionReturnDecoder.decode(
                ethCall.getValue(),
                function.getOutputParameters()
        );

        if (results.isEmpty()) {
            return null;
        }
        return (T) results.get(0).getValue();
    }

    /**
     * 执行调用原始结果
     */
    private String executeCallRaw(Function function) throws ExecutionException, InterruptedException {
        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            credentials.getAddress(),
                            contractAddress,
                            encodedFunction(function)
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();
        } catch (IOException e) {
            throw new ExecutionException("Failed to call contract", e);
        }

        return ethCall.getValue();
    }

    private String encodedFunction(Function function) {
        return FunctionEncoder.encode(function);
    }

    /**
     * 创建合约实例
     */
    public static NFTAsset load(String contractAddress, Web3j web3j, Credentials credentials) {
        return new NFTAsset(contractAddress, web3j, credentials);
    }

    /**
     * 部署合约（简化版本，实际部署需要使用 TransactionManager）
     */
    public static NFTAsset deploy(Web3j web3j, Credentials credentials,
                                   ContractGasProvider gasProvider) throws Exception {
        // 由于合约部署需要复杂的交易管理，这里返回一个未初始化的合约实例
        // 实际部署建议使用 web3j CLI 工具或 Hardhat 部署脚本
        throw new UnsupportedOperationException(
                "合约部署请使用 Hardhat 部署脚本。部署后使用 load() 方法加载合约实例。"
        );
    }
}