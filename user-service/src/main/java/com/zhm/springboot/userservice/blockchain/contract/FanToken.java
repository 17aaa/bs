package com.zhm.springboot.userservice.blockchain.contract;

import lombok.Getter;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * FanToken 智能合约调用工具类
 * ERC-20 粉丝代币合约
 */
public class FanToken {

    @Getter
    private final String contractAddress;
    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;

    public FanToken(String contractAddress, Web3j web3j, Credentials credentials) {
        this(contractAddress, web3j, credentials, new DefaultGasProvider());
    }

    public FanToken(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        this.contractAddress = contractAddress;
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /**
     * 获取代币名称
     */
    public String name() throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "name",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Utf8String>() {})
        );
        return executeCall(function);
    }

    /**
     * 获取代币符号
     */
    public String symbol() throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "symbol",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Utf8String>() {})
        );
        return executeCall(function);
    }

    /**
     * 获取总供应量
     */
    public BigInteger totalSupply() throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "totalSupply",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 获取账户余额
     */
    public BigInteger balanceOf(String account) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "balanceOf",
                Arrays.asList(new Address(account)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 转账
     */
    public TransactionReceipt transfer(String recipient, BigInteger amount) throws Exception {
        final Function function = new Function(
                "transfer",
                Arrays.asList(new Address(recipient), new Uint256(amount)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 授权
     */
    public TransactionReceipt approve(String spender, BigInteger amount) throws Exception {
        final Function function = new Function(
                "approve",
                Arrays.asList(new Address(spender), new Uint256(amount)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 授权额度查询
     */
    public BigInteger allowance(String owner, String spender) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "allowance",
                Arrays.asList(new Address(owner), new Address(spender)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 从授权账户转账
     */
    public TransactionReceipt transferFrom(String sender, String recipient, BigInteger amount) throws Exception {
        final Function function = new Function(
                "transferFrom",
                Arrays.asList(new Address(sender), new Address(recipient), new Uint256(amount)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 参与公募
     */
    public TransactionReceipt participateInSale(BigInteger amount) throws Exception {
        final Function function = new Function(
                "participateInSale",
                Arrays.asList(new Uint256(amount)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 质押代币
     */
    public TransactionReceipt stake(BigInteger amount) throws Exception {
        final Function function = new Function(
                "stake",
                Arrays.asList(new Uint256(amount)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 解除质押
     */
    public TransactionReceipt unstake(BigInteger amount) throws Exception {
        final Function function = new Function(
                "unstake",
                Arrays.asList(new Uint256(amount)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 领取奖励
     */
    public TransactionReceipt getReward() throws Exception {
        final Function function = new Function(
                "getReward",
                Collections.emptyList(),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 查询质押余额
     */
    public BigInteger stakedBalance(String account) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "stakedBalance",
                Arrays.asList(new Address(account)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 查询待领取奖励
     */
    public BigInteger earned(String account) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "earned",
                Arrays.asList(new Address(account)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
        return executeCall(function);
    }

    /**
     * 执行交易（写入操作）
     */
    private TransactionReceipt executeTransaction(Function function) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);
        throw new UnsupportedOperationException(
                "交易执行需要使用 TransactionManager，请使用 Web3j 的 TransactionManager 实现"
        );
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
     * 创建合约实例
     */
    public static FanToken load(String contractAddress, Web3j web3j, Credentials credentials) {
        return new FanToken(contractAddress, web3j, credentials);
    }
}