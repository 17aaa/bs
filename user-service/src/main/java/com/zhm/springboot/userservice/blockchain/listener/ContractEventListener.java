package com.zhm.springboot.userservice.blockchain.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhm.springboot.userservice.blockchain.config.ContractProperties;
import com.zhm.springboot.userservice.market.entity.MarketOrder;
import com.zhm.springboot.userservice.market.mapper.MarketOrderMapper;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftVersion;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.nft.mapper.NftVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 智能合约事件监听器
 * 通过轮询日志方式监听链上事件并同步到数据库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractEventListener {

    private final ContractProperties contractProperties;
    private final Web3j web3j;
    private final NftAssetMapper nftAssetMapper;
    private final NftVersionMapper nftVersionMapper;
    private final MarketOrderMapper marketOrderMapper;

    // ERC-721 Transfer 事件签名
    private static final Event TRANSFER_EVENT = new Event(
            "Transfer",
            Arrays.asList(
                    new TypeReference<Address>(true) {},  // from (indexed)
                    new TypeReference<Address>(true) {},  // to (indexed)
                    new TypeReference<Uint256>(true) {}   // tokenId (indexed)
            )
    );

    // MicroMarket SaleExecuted 事件签名
    private static final Event SALE_EXECUTED_EVENT = new Event(
            "SaleExecuted",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {},  // saleId (indexed)
                    new TypeReference<Address>(false) {}, // buyer
                    new TypeReference<Uint256>(false) {}  // price
            )
    );

    // NFTAsset VersionUpdated 事件签名
    private static final Event VERSION_UPDATED_EVENT = new Event(
            "VersionUpdated",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {},   // tokenId (indexed)
                    new TypeReference<org.web3j.abi.datatypes.Utf8String>(false) {}, // metadataHash
                    new TypeReference<Uint256>(false) {},  // version
                    new TypeReference<Uint256>(false) {}   // timestamp
            )
    );

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void startListening() {
        log.info("Contract event listener starting...");

        String nftAddress = contractProperties.getNftAssetAddress();
        String marketAddress = contractProperties.getMicroMarketAddress();

        if (nftAddress == null || nftAddress.isBlank()) {
            log.warn("NFT contract address not configured, skipping event listener");
            return;
        }

        listenToTransferEvents(nftAddress);
        listenToVersionUpdatedEvents(nftAddress);

        if (marketAddress != null && !marketAddress.isBlank()) {
            listenToSaleExecutedEvents(marketAddress);
        }
    }

    /**
     * 监听 ERC-721 Transfer 事件，同步 NFT 所有权变更到数据库
     */
    private void listenToTransferEvents(String nftContractAddress) {
        try {
            String encodedTopic = EventEncoder.encode(TRANSFER_EVENT);
            EthFilter filter = new EthFilter(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST,
                    nftContractAddress
            ).addSingleTopic(encodedTopic);

            web3j.ethLogFlowable(filter).subscribe(
                    log -> handleTransferEvent(log, nftContractAddress),
                    error -> log.error("Transfer event listener error: {}", error.getMessage())
            );
            log.info("Transfer event listener registered for contract: {}", nftContractAddress);
        } catch (Exception e) {
            log.error("Failed to register Transfer event listener: {}", e.getMessage());
        }
    }

    /**
     * 监听 VersionUpdated 事件，同步版本历史到数据库
     */
    private void listenToVersionUpdatedEvents(String nftContractAddress) {
        try {
            String encodedTopic = EventEncoder.encode(VERSION_UPDATED_EVENT);
            EthFilter filter = new EthFilter(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST,
                    nftContractAddress
            ).addSingleTopic(encodedTopic);

            web3j.ethLogFlowable(filter).subscribe(
                    log -> handleVersionUpdatedEvent(log),
                    error -> log.error("VersionUpdated event listener error: {}", error.getMessage())
            );
            log.info("VersionUpdated event listener registered for contract: {}", nftContractAddress);
        } catch (Exception e) {
            log.error("Failed to register VersionUpdated event listener: {}", e.getMessage());
        }
    }

    /**
     * 监听 SaleExecuted 事件，同步订单状态到数据库
     */
    private void listenToSaleExecutedEvents(String marketContractAddress) {
        try {
            String encodedTopic = EventEncoder.encode(SALE_EXECUTED_EVENT);
            EthFilter filter = new EthFilter(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST,
                    marketContractAddress
            ).addSingleTopic(encodedTopic);

            web3j.ethLogFlowable(filter).subscribe(
                    log -> handleSaleExecutedEvent(log),
                    error -> log.error("SaleExecuted event listener error: {}", error.getMessage())
            );
            log.info("SaleExecuted event listener registered for contract: {}", marketContractAddress);
        } catch (Exception e) {
            log.error("Failed to register SaleExecuted event listener: {}", e.getMessage());
        }
    }

    /**
     * 处理 Transfer 事件：更新 nft_assets 表中的 owner_address
     */
    private void handleTransferEvent(org.web3j.protocol.core.methods.response.Log eventLog,
                                      String nftContractAddress) {
        try {
            List<String> topics = eventLog.getTopics();
            if (topics.size() < 4) return;

            // topics[0] = event signature
            // topics[1] = from address (indexed)
            // topics[2] = to address (indexed)
            // topics[3] = tokenId (indexed)
            String toAddress = "0x" + topics.get(2).substring(26); // 去掉 padding
            BigInteger tokenId = new BigInteger(topics.get(3).substring(2), 16);

            // 跳过铸造事件（from = 0x000...）
            String fromAddress = "0x" + topics.get(1).substring(26);
            if (fromAddress.equals("0x0000000000000000000000000000000000000000")) {
                return;
            }

            // 更新数据库中的 owner_address
            LambdaQueryWrapper<NftAsset> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(NftAsset::getTokenId, tokenId.longValue())
                   .eq(NftAsset::getContractAddress, nftContractAddress);

            NftAsset nftAsset = nftAssetMapper.selectOne(wrapper);
            if (nftAsset != null) {
                nftAsset.setOwnerAddress(toAddress);
                nftAssetMapper.updateById(nftAsset);
                log.info("NFT ownership synced: tokenId={}, newOwner={}", tokenId, toAddress);
            }
        } catch (Exception e) {
            log.error("Failed to handle Transfer event: {}", e.getMessage());
        }
    }

    /**
     * 处理 VersionUpdated 事件：写入 nft_versions 表
     */
    private void handleVersionUpdatedEvent(org.web3j.protocol.core.methods.response.Log eventLog) {
        try {
            List<String> topics = eventLog.getTopics();
            if (topics.size() < 2) return;

            BigInteger tokenId = new BigInteger(topics.get(1).substring(2), 16);

            // 解码非 indexed 数据（data 字段）
            String data = eventLog.getData();
            if (data == null || data.length() < 2) return;

            // 简单解析：version 在 data 的第二个 uint256 位置
            // 完整解析需要 ABI decoder，这里记录基本信息
            log.info("VersionUpdated event received for tokenId={}, txHash={}",
                    tokenId, eventLog.getTransactionHash());

            // 检查版本是否已存在（避免重复写入）
            LambdaQueryWrapper<NftAsset> nftWrapper = new LambdaQueryWrapper<>();
            nftWrapper.eq(NftAsset::getTokenId, tokenId.longValue());
            NftAsset nft = nftAssetMapper.selectOne(nftWrapper);
            if (nft == null) return;

            LambdaQueryWrapper<NftVersion> versionWrapper = new LambdaQueryWrapper<>();
            versionWrapper.eq(NftVersion::getNftAssetId, nft.getId())
                          .eq(NftVersion::getTxHash, eventLog.getTransactionHash());
            if (nftVersionMapper.selectCount(versionWrapper) > 0) return;

            NftVersion version = new NftVersion();
            version.setNftAssetId(nft.getId());
            version.setTxHash(eventLog.getTransactionHash());
            version.setBlockNumber(eventLog.getBlockNumber() != null
                    ? eventLog.getBlockNumber().longValue() : null);
            version.setCreatedAt(LocalDateTime.now());
            nftVersionMapper.insert(version);

        } catch (Exception e) {
            log.error("Failed to handle VersionUpdated event: {}", e.getMessage());
        }
    }

    /**
     * 处理 SaleExecuted 事件：将订单状态更新为已售
     */
    private void handleSaleExecutedEvent(org.web3j.protocol.core.methods.response.Log eventLog) {
        try {
            List<String> topics = eventLog.getTopics();
            if (topics.size() < 2) return;

            BigInteger saleId = new BigInteger(topics.get(1).substring(2), 16);
            String txHash = eventLog.getTransactionHash();

            // 查找对应的本地订单（通过 sale_id 匹配）
            LambdaQueryWrapper<MarketOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MarketOrder::getSaleId, saleId.longValue())
                   .eq(MarketOrder::getStatus, 1); // 只更新活跃订单

            MarketOrder order = marketOrderMapper.selectOne(wrapper);
            if (order != null && order.getStatus() == 1) {
                order.setStatus(2); // 已售
                order.setTxHash(txHash);
                order.setUpdatedAt(LocalDateTime.now());
                marketOrderMapper.updateById(order);
                log.info("Order status synced from chain: saleId={}, txHash={}", saleId, txHash);
            }
        } catch (Exception e) {
            log.error("Failed to handle SaleExecuted event: {}", e.getMessage());
        }
    }
}
