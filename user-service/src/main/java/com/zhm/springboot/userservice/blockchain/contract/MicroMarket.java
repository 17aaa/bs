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
 * MicroMarket 智能合约调用工具类
 * NFT 交易市场合约
 */
public class MicroMarket {

    @Getter
    private final String contractAddress;
    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;

    public MicroMarket(String contractAddress, Web3j web3j, Credentials credentials) {
        this(contractAddress, web3j, credentials, new DefaultGasProvider());
    }

    public MicroMarket(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        this.contractAddress = contractAddress;
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /**
     * 创建固定价格销售
     */
    public TransactionReceipt createFixedPriceSale(
            String nftContract,
            BigInteger tokenId,
            BigInteger price,
            String paymentToken
    ) throws Exception {
        final Function function = new Function(
                "createFixedPriceSale",
                Arrays.asList(
                        new Address(nftContract),
                        new Uint256(tokenId),
                        new Uint256(price),
                        new Address(paymentToken != null ? paymentToken : "0x0000000000000000000000000000000000000000")
                ),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 创建荷兰拍卖
     */
    public TransactionReceipt createDutchAuction(
            String nftContract,
            BigInteger tokenId,
            BigInteger startPrice,
            BigInteger reservePrice,
            BigInteger startTime,
            BigInteger duration
    ) throws Exception {
        final Function function = new Function(
                "createDutchAuction",
                Arrays.asList(
                        new Address(nftContract),
                        new Uint256(tokenId),
                        new Uint256(startPrice),
                        new Uint256(reservePrice),
                        new Uint256(startTime),
                        new Uint256(duration)
                ),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 购买 NFT
     */
    public TransactionReceipt buy(BigInteger saleId) throws Exception {
        final Function function = new Function(
                "buy",
                Arrays.asList(new Uint256(saleId)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 取消销售
     */
    public TransactionReceipt cancelSale(BigInteger saleId) throws Exception {
        final Function function = new Function(
                "cancelSale",
                Arrays.asList(new Uint256(saleId)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 创建报价
     */
    public TransactionReceipt createOffer(
            String nftContract,
            BigInteger tokenId,
            BigInteger price,
            String paymentToken,
            BigInteger expirationTime
    ) throws Exception {
        final Function function = new Function(
                "createOffer",
                Arrays.asList(
                        new Address(nftContract),
                        new Uint256(tokenId),
                        new Uint256(price),
                        new Address(paymentToken != null ? paymentToken : "0x0000000000000000000000000000000000000000"),
                        new Uint256(expirationTime)
                ),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 接受报价
     */
    public TransactionReceipt acceptOffer(BigInteger offerId) throws Exception {
        final Function function = new Function(
                "acceptOffer",
                Arrays.asList(new Uint256(offerId)),
                Collections.emptyList()
        );
        return executeTransaction(function);
    }

    /**
     * 获取销售信息
     */
    public SaleInfo getSale(BigInteger saleId) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "getSale",
                Arrays.asList(new Uint256(saleId)),
                Arrays.asList(
                        new TypeReference<Address>() {},  // seller
                        new TypeReference<Address>() {},  // nftContract
                        new TypeReference<Uint256>() {},  // tokenId
                        new TypeReference<Uint256>() {},  // price
                        new TypeReference<Address>() {},  // paymentToken
                        new TypeReference<Uint256>() {},  // startTime
                        new TypeReference<Uint256>() {},  // endTime
                        new TypeReference<Bool>() {}      // active
                )
        );
        List<Type> results = executeCallMultiple(function);
        if (results == null || results.size() < 8) {
            return null;
        }
        return new SaleInfo(
                (String) results.get(0).getValue(),  // seller
                (String) results.get(1).getValue(),  // nftContract
                ((BigInteger) results.get(2).getValue()),  // tokenId
                ((BigInteger) results.get(3).getValue()),  // price
                (String) results.get(4).getValue(),  // paymentToken
                ((BigInteger) results.get(5).getValue()),  // startTime
                ((BigInteger) results.get(6).getValue()),  // endTime
                (Boolean) results.get(7).getValue()   // active
        );
    }

    /**
     * 获取当前价格（荷兰拍卖适用）
     */
    public BigInteger getCurrentPrice(BigInteger saleId) throws ExecutionException, InterruptedException {
        final Function function = new Function(
                "getCurrentPrice",
                Arrays.asList(new Uint256(saleId)),
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
     * 执行调用返回多个值
     */
    private List<Type> executeCallMultiple(Function function) throws ExecutionException, InterruptedException {
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

        return FunctionReturnDecoder.decode(
                ethCall.getValue(),
                function.getOutputParameters()
        );
    }

    /**
     * 销售信息
     */
    public static class SaleInfo {
        public final String seller;
        public final String nftContract;
        public final BigInteger tokenId;
        public final BigInteger price;
        public final String paymentToken;
        public final BigInteger startTime;
        public final BigInteger endTime;
        public final Boolean active;

        public SaleInfo(String seller, String nftContract, BigInteger tokenId,
                        BigInteger price, String paymentToken, BigInteger startTime,
                        BigInteger endTime, Boolean active) {
            this.seller = seller;
            this.nftContract = nftContract;
            this.tokenId = tokenId;
            this.price = price;
            this.paymentToken = paymentToken;
            this.startTime = startTime;
            this.endTime = endTime;
            this.active = active;
        }
    }

    /**
     * 创建合约实例
     */
    public static MicroMarket load(String contractAddress, Web3j web3j, Credentials credentials) {
        return new MicroMarket(contractAddress, web3j, credentials);
    }
}