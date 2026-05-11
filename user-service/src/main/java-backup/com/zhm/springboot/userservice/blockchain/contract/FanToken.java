package com.zhm.springboot.userservice.blockchain.contract;

import io.reactivex.Flowable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
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
public class FanToken extends Contract {
    public static final String BINARY = "TODO: Load from compiled JSON";

    public static final String FUNC_CONFIGURESALE = "configureSale";
    public static final String FUNC_PARTICIPATEINSALE = "participateInSale";
    public static final String FUNC_STAKE = "stake";
    public static final String FUNC_UNSTAKE = "unstake";
    public static final String FUNC_GETREWARD = "getReward";
    public static final String FUNC_CLAIMVESTED = "claimVested";
    public static final String FUNC_GETPENDINGREWARD = "getPendingReward";
    public static final String FUNC_GETVESTEDAMOUNT = "getVestedAmount";
    public static final String FUNC_SETREWARDRATE = "setRewardRate";
    public static final String FUNC_BALANCEOF = "balanceOf";
    public static final String FUNC_TRANSFER = "transfer";
    public static final String FUNC_BURN = "burn";

    public static final Event EVENT_TOKENSPURCHASED = new Event("TokensPurchased",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_STAKED = new Event("Staked",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_UNSTAKED = new Event("Unstaked",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_REWARDCLAIMED = new Event("RewardClaimed",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_VESTINGRELEASED = new Event("VestingReleased",
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>() {}));

    public static final Event EVENT_SALECONFIGUPDATED = new Event("SaleConfigUpdated",
            Arrays.<TypeReference<?>>asList());

    private static final String TYPE = "FanToken";

    protected FanToken(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected FanToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteCall<TransactionReceipt> configureSale(
            BigInteger pricePerToken,
            BigInteger minPurchase,
            BigInteger maxPurchase,
            BigInteger startTime,
            BigInteger endTime) {
        final Function function = new Function(
                FUNC_CONFIGURESALE,
                Arrays.<Type>asList(
                        new Uint256(pricePerToken),
                        new Uint256(minPurchase),
                        new Uint256(maxPurchase),
                        new Uint256(startTime),
                        new Uint256(endTime)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> participateInSale(BigInteger amount, BigInteger value) {
        final Function function = new Function(
                FUNC_PARTICIPATEINSALE,
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, value);
    }

    public RemoteCall<TransactionReceipt> stake(BigInteger amount) {
        final Function function = new Function(
                FUNC_STAKE,
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> unstake(BigInteger amount) {
        final Function function = new Function(
                FUNC_UNSTAKE,
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> getReward() {
        final Function function = new Function(
                FUNC_GETREWARD,
                Collections.<Type>emptyList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> claimVested() {
        final Function function = new Function(
                FUNC_CLAIMVESTED,
                Collections.<Type>emptyList(),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getPendingReward(String user) {
        final Function function = new Function(FUNC_GETPENDINGREWARD,
                Arrays.<Type>asList(new Address(user)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> getVestedAmount(String user) {
        final Function function = new Function(FUNC_GETVESTEDAMOUNT,
                Arrays.<Type>asList(new Address(user)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> setRewardRate(BigInteger rewardRate) {
        final Function function = new Function(
                FUNC_SETREWARDRATE,
                Arrays.<Type>asList(new Uint256(rewardRate)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> balanceOf(String owner) {
        final Function function = new Function(FUNC_BALANCEOF,
                Arrays.<Type>asList(new Address(owner)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> transfer(String recipient, BigInteger amount) {
        final Function function = new Function(
                FUNC_TRANSFER,
                Arrays.<Type>asList(new Address(recipient), new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> burn(BigInteger amount) {
        final Function function = new Function(
                FUNC_BURN,
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> creator() {
        final Function function = new Function("creator",
                Collections.<Type>emptyList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> totalSupply() {
        final Function function = new Function("totalSupply",
                Collections.<Type>emptyList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static RemoteCall<FanToken> deploy(
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider gasProvider,
            String name,
            String symbol,
            String creator,
            BigInteger totalSupply,
            Tuple allocation) {
        return deployRemoteCall(FanToken.class, web3j, credentials, gasProvider, BINARY,
                name, symbol, creator, totalSupply, allocation);
    }

    public static RemoteCall<FanToken> load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new RemoteCall<>(() -> new FanToken(contractAddress, web3j, credentials, gasProvider));
    }

    public static class TokensPurchasedEventResponse extends BaseEventResponse {
        public String buyer;
        public BigInteger amount;
        public BigInteger cost;
    }

    public static class StakedEventResponse extends BaseEventResponse {
        public String user;
        public BigInteger amount;
    }

    public static class UnstakedEventResponse extends BaseEventResponse {
        public String user;
        public BigInteger amount;
    }

    public static class RewardClaimedEventResponse extends BaseEventResponse {
        public String user;
        public BigInteger amount;
    }

    public static class VestingReleasedEventResponse extends BaseEventResponse {
        public String beneficiary;
        public BigInteger amount;
    }

    public static class TokenAllocation {
        public BigInteger publicSale;
        public BigInteger creator;
        public BigInteger team;
        public BigInteger staking;
    }

    public static class VestingInfo {
        public String beneficiary;
        public BigInteger totalAmount;
        public BigInteger released;
        public BigInteger startTime;
        public BigInteger cliffDuration;
        public BigInteger vestingDuration;
    }

    public static class StakeInfo {
        public BigInteger stakedAmount;
        public BigInteger rewardDebt;
        public BigInteger lastUpdateTime;
    }

    public static class SaleConfig {
        public BigInteger pricePerToken;
        public BigInteger minPurchase;
        public BigInteger maxPurchase;
        public BigInteger startTime;
        public BigInteger endTime;
        public boolean active;
    }
}