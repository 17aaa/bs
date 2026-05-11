package com.zhm.springboot.userservice.blockchain.contract;

import io.reactivex.Flowable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuple.Tuple;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.*;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 */
public class MicroMarket extends Contract {
    public static final String BINARY = "TODO: Load from compiled JSON";

    public static final String FUNC_CREATEFIXEDPRICESALE = "createFixedPriceSale";
    public static final String FUNC_CREATEDUTCHACTION = "createDutchAuction";
    public static final String FUNC_BUY = "buy";
    public static final String FUNC_CANCELSALE = "cancelSale";
    public static final String FUNC_CREATEOFFER = "createOffer";
    public static final String FUNC_ACCEPTOFFER = "acceptOffer";
    public static final String FUNC_CANCELOFFER = "cancelOffer";
    public static final String FUNC_GETSALE = "getSale";
    public static final String FUNC_GETCURRENTPRICE = "getCurrentPrice";
    public static final String FUNC_WITHDRAW = "withdraw";

    public static final Event EVENT_SALECREATED = new Event("SaleCreated",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Uint8>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_SALEEXECUTED = new Event("SaleExecuted",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_SALECANCELLED = new Event("SaleCancelled",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));

    public static final Event EVENT_OFFERCREATED = new Event("OfferCreated",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_OFFERACCEPTED = new Event("OfferAccepted",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {}));

    public static final Event EVENT_OFFERCANCELLED = new Event("OfferCancelled",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));

    private static final String TYPE = "MicroMarket";

    protected MicroMarket(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected MicroMarket(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteCall<BigInteger> createFixedPriceSale(
            String nftContract,
            BigInteger tokenId,
            BigInteger price,
            String paymentToken,
            BigInteger duration,
            BigInteger royaltyFee,
            String royaltyRecipient) {
        final Function function = new Function(
                FUNC_CREATEFIXEDPRICESALE,
                Arrays.<Type>asList(
                        new Address(nftContract),
                        new Uint256(tokenId),
                        new Uint256(price),
                        new Address(paymentToken),
                        new Uint256(duration),
                        new Uint256(royaltyFee),
                        new Address(royaltyRecipient)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> createFixedPriceSaleAndGetReceipt(
            String nftContract,
            BigInteger tokenId,
            BigInteger price,
            String paymentToken,
            BigInteger duration,
            BigInteger royaltyFee,
            String royaltyRecipient) {
        return executeRemoteCallTransaction(function ->
            createFixedPriceSale(nftContract, tokenId, price, paymentToken, duration, royaltyFee, royaltyRecipient));
    }

    public RemoteCall<BigInteger> createDutchAuction(
            String nftContract,
            BigInteger tokenId,
            BigInteger startPrice,
            BigInteger reservePrice,
            String paymentToken,
            BigInteger duration,
            BigInteger royaltyFee,
            String royaltyRecipient) {
        final Function function = new Function(
                FUNC_CREATEDUTCHACTION,
                Arrays.<Type>asList(
                        new Address(nftContract),
                        new Uint256(tokenId),
                        new Uint256(startPrice),
                        new Uint256(reservePrice),
                        new Address(paymentToken),
                        new Uint256(duration),
                        new Uint256(royaltyFee),
                        new Address(royaltyRecipient)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> buy(BigInteger saleId, BigInteger value) {
        final Function function = new Function(
                FUNC_BUY,
                Arrays.<Type>asList(new Uint256(saleId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, value);
    }

    public RemoteCall<TransactionReceipt> cancelSale(BigInteger saleId) {
        final Function function = new Function(
                FUNC_CANCELSALE,
                Arrays.<Type>asList(new Uint256(saleId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> createOffer(
            String nftContract,
            BigInteger tokenId,
            BigInteger price,
            String paymentToken,
            BigInteger duration) {
        final Function function = new Function(
                FUNC_CREATEOFFER,
                Arrays.<Type>asList(
                        new Address(nftContract),
                        new Uint256(tokenId),
                        new Uint256(price),
                        new Address(paymentToken),
                        new Uint256(duration)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> acceptOffer(BigInteger offerId) {
        final Function function = new Function(
                FUNC_ACCEPTOFFER,
                Arrays.<Type>asList(new Uint256(offerId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> cancelOffer(BigInteger offerId) {
        final Function function = new Function(
                FUNC_CANCELOFFER,
                Arrays.<Type>asList(new Uint256(offerId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Sale> getSale(BigInteger saleId) {
        final Function function = new Function(FUNC_GETSALE,
                Arrays.<Type>asList(new Uint256(saleId)),
                Arrays.<TypeReference<?>>asList());
        return executeRemoteCallMultipleValueReturn(function, Sale.class);
    }

    public RemoteCall<BigInteger> getCurrentPrice(BigInteger saleId) {
        final Function function = new Function(FUNC_GETCURRENTPRICE,
                Arrays.<Type>asList(new Uint256(saleId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> withdraw() {
        final Function function = new Function(
                FUNC_WITHDRAW,
                Collections.<Type>emptyList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<MicroMarket> deploy(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return deployRemoteCall(MicroMarket.class, web3j, credentials, gasProvider, BINARY, "");
    }

    public static RemoteCall<MicroMarket> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return deployRemoteCall(MicroMarket.class, web3j, transactionManager, gasProvider, BINARY, "");
    }

    public static RemoteCall<MicroMarket> load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new RemoteCall<>(() -> new MicroMarket(contractAddress, web3j, credentials, gasProvider));
    }

    public static RemoteCall<MicroMarket> load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return new RemoteCall<>(() -> new MicroMarket(contractAddress, web3j, transactionManager, gasProvider));
    }

    public static class SaleCreatedEventResponse extends BaseEventResponse {
        public BigInteger saleId;
        public String seller;
        public String nftContract;
        public BigInteger tokenId;
        public BigInteger saleType;
        public BigInteger price;
        public BigInteger endTime;
    }

    public static class SaleExecutedEventResponse extends BaseEventResponse {
        public BigInteger saleId;
        public String seller;
        public String buyer;
        public BigInteger tokenId;
        public BigInteger price;
    }

    public static class SaleCancelledEventResponse extends BaseEventResponse {
        public BigInteger saleId;
    }

    public static class OfferCreatedEventResponse extends BaseEventResponse {
        public BigInteger offerId;
        public String buyer;
        public String nftContract;
        public BigInteger tokenId;
        public BigInteger price;
    }

    public static class OfferAcceptedEventResponse extends BaseEventResponse {
        public BigInteger offerId;
        public String seller;
    }

    public static class OfferCancelledEventResponse extends BaseEventResponse {
        public BigInteger offerId;
    }

    public static class Sale {
        public BigInteger saleId;
        public String seller;
        public String nftContract;
        public BigInteger tokenId;
        public BigInteger saleType;
        public BigInteger price;
        public BigInteger startPrice;
        public BigInteger reservePrice;
        public BigInteger startTime;
        public BigInteger endTime;
        public String paymentToken;
        public BigInteger royaltyFee;
        public String royaltyRecipient;
        public boolean active;
        public boolean completed;
    }
}