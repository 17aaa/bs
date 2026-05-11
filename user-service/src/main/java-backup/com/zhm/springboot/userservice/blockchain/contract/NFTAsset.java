package com.zhm.springboot.userservice.blockchain.contract;

import io.reactivex.Flowable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint96;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuple.Tuple;
import org.web3j.tuple.generated.Tuple10;
import org.web3j.tuple.generated.Tuple2;
import org.web3j.tuple.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class NFTAsset extends Contract {
    public static final String BINARY = "TODO: Load from compiled JSON";

    public static final String FUNC_APPROVE = "approve";
    public static final String FUNC_BALANCEOF = "balanceOf";
    public static final String FUNC_BURN = "burn";
    public static final String FUNC_GETCURRENTVERSION = "getCurrentVersion";
    public static final String FUNC_GETROYALTYINFO = "getRoyaltyInfo";
    public static final String FUNC_GETVERSIONHISTORY = "getVersionHistory";
    public static final String FUNC_MINT = "mint";
    public static final String FUNC_OWNEROF = "ownerOf";
    public static final String FUNC_ROYALTYAMOUNT = "royaltyAmount";
    public static final String FUNC_SAFETRANSFERFROM = "safeTransferFrom";
    public static final String FUNC_SETROYALTY = "setRoyalty";
    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";
    public static final String FUNC_TOKENURI = "tokenURI";
    public static final String FUNC_TRANSFERFROM = "transferFrom";
    public static final String FUNC_UPDATEMETADATA = "updateMetadata";

    public static final Event EVENT_APPROVAL = new Event("Approval",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    public static final Event EVENT_APPROVALFORALL = new Event("ApprovalForAll",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Bool>() {}));
    public static final Event EVENT_OWNERSHIPTRANSFERRED = new Event("OwnershipTransferred",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    public static final Event EVENT_ROYALTYSET = new Event("RoyaltySet",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint96>() {}));
    public static final Event EVENT_TRANSFER = new Event("Transfer",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    public static final Event EVENT_VERSIONUPDATED = new Event("VersionUpdated",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));

    ;

    private static final String TYPE = "NFTAsset";

    protected NFTAsset(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected NFTAsset(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public List<VersionUpdatedEventResponse> getVersionUpdatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = this.extractEventParametersWithLog(EVENT_VERSIONUPDATED, transactionReceipt);
        ArrayList<VersionUpdatedEventResponse> responses = new ArrayList<>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            VersionUpdatedEventResponse typedResponse = new VersionUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newMetadataHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<VersionUpdatedEventResponse> versionUpdatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> {
            VersionUpdatedEventResponse response = new VersionUpdatedEventResponse();
            response.log = log;
            response.tokenId = (BigInteger) ((Address) log.getTopics().get(1)).getValue();
            response.newMetadataHash = ((Bytes32) log.getData()).getValue();
            response.version = ((Uint256) log.getData()).getValue();
            response.timestamp = ((Uint256) log.getData()).getValue();
            return response;
        });
    }

    public RemoteCall<BigInteger> mint(String to, String metadataHash) {
        final Function function = new Function(
                FUNC_MINT,
                Arrays.<Type>asList(new Address(to), new Utf8String(metadataHash)),
                Collections.<TypeReference<?>>emptyList(),
                new TypeReference<Uint256>() {});
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> mintAndGetReceipt(String to, String metadataHash) {
        return executeRemoteCallTransaction(function -> mint(to, metadataHash));
    }

    public RemoteCall<BigInteger> getCurrentVersion(BigInteger tokenId) {
        final Function function = new Function(FUNC_GETCURRENTVERSION,
                Arrays.<Type>asList(new Uint256(tokenId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<List<TokenVersion>> getVersionHistory(BigInteger tokenId) {
        final Function function = new Function(FUNC_GETVERSIONHISTORY,
                Arrays.<Type>asList(new Uint256(tokenId)),
                Arrays.<TypeReference<?>>asList());
        return executeRemoteCallMultipleValueReturn(function, TokenVersion.class);
    }

    public RemoteCall<Tuple2<String, BigInteger>> getRoyaltyInfo(BigInteger tokenId) {
        final Function function = new Function(FUNC_GETROYALTYINFO,
                Arrays.<Type>asList(new Uint256(tokenId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint96>() {}));
        return executeRemoteCallTupleReturn(function);
    }

    public RemoteCall<TransactionReceipt> setRoyalty(BigInteger tokenId, String recipient, BigInteger feeNumerator) {
        final Function function = new Function(
                FUNC_SETROYALTY,
                Arrays.<Type>asList(new Uint256(tokenId), new Address(recipient), new Uint96(feeNumerator)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> updateMetadata(BigInteger tokenId, String newMetadataHash) {
        final Function function = new Function(
                FUNC_UPDATEMETADATA,
                Arrays.<Type>asList(new Uint256(tokenId), new Utf8String(newMetadataHash)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> burn(BigInteger tokenId) {
        final Function function = new Function(
                FUNC_BURN,
                Arrays.<Type>asList(new Uint256(tokenId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> tokenURI(BigInteger tokenId) {
        final Function function = new Function(FUNC_TOKENURI,
                Arrays.<Type>asList(new Uint256(tokenId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> royaltyAmount(BigInteger tokenId, BigInteger salePrice) {
        final Function function = new Function(FUNC_ROYALTYAMOUNT,
                Arrays.<Type>asList(new Uint256(tokenId), new Uint256(salePrice)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> ownerOf(BigInteger tokenId) {
        final Function function = new Function(FUNC_OWNEROF,
                Arrays.<Type>asList(new Uint256(tokenId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> balanceOf(String owner) {
        final Function function = new Function(FUNC_BALANCEOF,
                Arrays.<Type>asList(new Address(owner)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> approve(String to, BigInteger tokenId) {
        final Function function = new Function(
                FUNC_APPROVE,
                Arrays.<Type>asList(new Address(to), new Uint256(tokenId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> safeTransferFrom(String from, String to, BigInteger tokenId) {
        final Function function = new Function(
                FUNC_SAFETRANSFERFROM,
                Arrays.<Type>asList(new Address(from), new Address(to), new Uint256(tokenId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> transferFrom(String from, String to, BigInteger tokenId) {
        final Function function = new Function(
                FUNC_TRANSFERFROM,
                Arrays.<Type>asList(new Address(from), new Address(to), new Uint256(tokenId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> supportsInterface(byte[] interfaceId) {
        final Function function = new Function(FUNC_SUPPORTSINTERFACE,
                Arrays.<Type>asList(new Bytes4(interfaceId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public static RemoteCall<NFTAsset> deploy(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return deployRemoteCall(NFTAsset.class, web3j, credentials, gasProvider, BINARY, "");
    }

    public static RemoteCall<NFTAsset> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return deployRemoteCall(NFTAsset.class, web3j, transactionManager, gasProvider, BINARY, "");
    }

    public static RemoteCall<NFTAsset> load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new RemoteCall<>(() -> new NFTAsset(contractAddress, web3j, credentials, gasProvider));
    }

    public static RemoteCall<NFTAsset> load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return new RemoteCall<>(() -> new NFTAsset(contractAddress, web3j, transactionManager, gasProvider));
    }

    public static class VersionUpdatedEventResponse extends BaseEventResponse {
        public BigInteger tokenId;
        public byte[] newMetadataHash;
        public BigInteger version;
        public BigInteger timestamp;
    }

    public static class TokenVersion {
        public BigInteger version;
        public String metadataHash;
        public String ipfsUri;
        public BigInteger timestamp;
        public String updater;
    }
}