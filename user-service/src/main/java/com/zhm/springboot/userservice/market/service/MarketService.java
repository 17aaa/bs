package com.zhm.springboot.userservice.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhm.springboot.userservice.audit.entity.AuditLog;
import com.zhm.springboot.userservice.audit.service.AuditLogService;
import com.zhm.springboot.userservice.blockchain.config.ContractProperties;
import com.zhm.springboot.userservice.entity.User;
import com.zhm.springboot.userservice.mapper.UserMapper;
import com.zhm.springboot.userservice.market.entity.MarketOrder;
import com.zhm.springboot.userservice.market.mapper.MarketOrderMapper;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.wallet.entity.Wallet;
import com.zhm.springboot.userservice.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 市场交易服务
 * 处理固定价格销售、荷兰拍卖的挂单、购买、取消逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketOrderMapper marketOrderMapper;
    private final NftAssetMapper nftAssetMapper;
    private final ContractProperties contractProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final AuditLogService auditLogService;

    private static final String ORDER_CACHE_KEY = "market:order:";

    /**
     * 创建固定价格销售订单
     */
    @Transactional(rollbackFor = Exception.class)
    public String createFixedPriceSale(String sellerAddress, String nftContract, Long tokenId,
                                        BigInteger price, Long nftAssetId, String paymentToken, Long endTime) throws Exception {
        // 验证 NFT 所有权（本地数据库）
        if (nftAssetId != null) {
            NftAsset nft = nftAssetMapper.selectById(nftAssetId);
            if (nft != null && !nft.getOwnerAddress().equalsIgnoreCase(sellerAddress)) {
                throw new SecurityException("Seller does not own the NFT: nftAssetId=" + nftAssetId);
            }
        }

        String orderId = UUID.randomUUID().toString();

        MarketOrder order = new MarketOrder();
        order.setOrderId(orderId);
        order.setSellerAddress(sellerAddress);
        order.setNftContract(nftContract != null ? nftContract : contractProperties.getNftAssetAddress());
        order.setTokenId(tokenId);
        order.setNftAssetId(nftAssetId);
        order.setOrderType(1);
        order.setStatus(1);
        order.setPrice(price);
        order.setPaymentToken(paymentToken != null ? paymentToken : "0x0000000000000000000000000000000000000000");
        order.setEndTime(endTime);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        marketOrderMapper.insert(order);

        // 使用数据库自增 ID 作为链上 SaleId（确定性、唯一）
        order.setSaleId(order.getId());
        marketOrderMapper.updateById(order);

        // 填充 NFT 信息冗余字段
        if (nftAssetId != null) {
            enrichOrderWithNftInfo(order, nftAssetId);
        }

        cacheOrder(order);

        log.info("Created fixed price sale: orderId={}, saleId={}", orderId, order.getId());

        auditLogService.log(null, AuditLog.ACTION_SELL, AuditLog.MODULE_MARKET,
                "创建固定价格订单: orderId=" + orderId + ", price=" + price);
        return orderId;
    }

    /**
     * 创建荷兰拍卖订单
     */
    @Transactional(rollbackFor = Exception.class)
    public String createDutchAuction(String sellerAddress, String nftContract, Long tokenId,
                                      BigInteger startPrice, BigInteger reservePrice,
                                      Long startTime, Long duration) throws Exception {
        if (reservePrice.compareTo(startPrice) >= 0) {
            throw new IllegalArgumentException("Reserve price must be less than start price");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        String orderId = UUID.randomUUID().toString();

        MarketOrder order = new MarketOrder();
        order.setOrderId(orderId);
        order.setSellerAddress(sellerAddress);
        order.setNftContract(nftContract != null ? nftContract : contractProperties.getNftAssetAddress());
        order.setTokenId(tokenId);
        order.setOrderType(2);
        order.setStatus(1);
        order.setStartPrice(startPrice);
        order.setReservePrice(reservePrice);
        order.setPrice(startPrice);
        order.setEndTime(startTime + duration);
        order.setPaymentToken("0x0000000000000000000000000000000000000000");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        marketOrderMapper.insert(order);

        order.setSaleId(order.getId());
        marketOrderMapper.updateById(order);

        cacheOrder(order);

        log.info("Created Dutch auction: orderId={}, startPrice={}, reservePrice={}, duration={}s",
                orderId, startPrice, reservePrice, duration);

        auditLogService.log(null, AuditLog.ACTION_SELL, AuditLog.MODULE_MARKET,
                "创建荷兰拍卖订单: orderId=" + orderId);
        return orderId;
    }

    /**
     * 购买 NFT
     */
    @Transactional(rollbackFor = Exception.class)
    public String buyNft(String orderId, String buyerAddress) throws Exception {
        MarketOrder order = getOrderById(orderId);

        if (order.getStatus() != 1) {
            throw new IllegalStateException("Order is not active");
        }
        if (order.getSellerAddress().equalsIgnoreCase(buyerAddress)) {
            throw new IllegalArgumentException("Seller cannot buy their own NFT");
        }
        if (order.getEndTime() != null && System.currentTimeMillis() / 1000 > order.getEndTime()) {
            order.setStatus(4);
            marketOrderMapper.updateById(order);
            throw new IllegalStateException("Order has expired");
        }

        BigInteger currentPrice = getCurrentPrice(order);

        // 平台积分支付：校验余额并转账
        User buyer = getUserByWalletAddress(buyerAddress);
        User seller = getUserByWalletAddress(order.getSellerAddress());

        if (buyer == null) throw new IllegalStateException("买家账户不存在");
        if (seller == null) throw new IllegalStateException("卖家账户不存在");

        BigInteger buyerBalance = buyer.getPlatformBalance() != null ? buyer.getPlatformBalance() : BigInteger.ZERO;
        if (buyerBalance.compareTo(currentPrice) < 0) {
            throw new IllegalStateException("平台积分余额不足，当前余额: " + toMatic(buyerBalance) + " 积分，需要: " + toMatic(currentPrice) + " 积分");
        }

        // 计算版税（仅二次销售时生效，创作者 != 卖家）
        BigInteger royaltyAmount = BigInteger.ZERO;
        String creatorAddress = null;
        if (order.getNftAssetId() != null) {
            NftAsset nftForRoyalty = nftAssetMapper.selectById(order.getNftAssetId());
            if (nftForRoyalty != null
                    && !nftForRoyalty.getCreatorAddress().equalsIgnoreCase(order.getSellerAddress())
                    && nftForRoyalty.getRoyaltyFee() != null && nftForRoyalty.getRoyaltyFee() > 0) {
                creatorAddress = nftForRoyalty.getCreatorAddress();
                royaltyAmount = currentPrice
                        .multiply(BigInteger.valueOf(nftForRoyalty.getRoyaltyFee()))
                        .divide(BigInteger.valueOf(10000));
            }
        }

        // 扣减买家，增加卖家（扣除版税后）
        buyer.setPlatformBalance(buyerBalance.subtract(currentPrice));
        seller.setPlatformBalance(
                (seller.getPlatformBalance() != null ? seller.getPlatformBalance() : BigInteger.ZERO)
                        .add(currentPrice.subtract(royaltyAmount)));
        userMapper.updateById(buyer);
        userMapper.updateById(seller);

        // 版税转给创作者
        if (creatorAddress != null && royaltyAmount.compareTo(BigInteger.ZERO) > 0) {
            User creator = getUserByWalletAddress(creatorAddress);
            if (creator != null) {
                creator.setPlatformBalance(
                        (creator.getPlatformBalance() != null ? creator.getPlatformBalance() : BigInteger.ZERO)
                                .add(royaltyAmount));
                userMapper.updateById(creator);
                log.info("Royalty paid: {} to creator {}", toMatic(royaltyAmount), creatorAddress);
            }
        }

        String txHash = generateTxHash(order.getId() * 10000L + System.currentTimeMillis() % 10000);

        order.setStatus(2);
        order.setBuyerAddress(buyerAddress);
        order.setFinalPrice(currentPrice);
        order.setTxHash(txHash);
        order.setUpdatedAt(LocalDateTime.now());

        marketOrderMapper.updateById(order);

        // 更新 NFT 所有者
        if (order.getNftAssetId() != null) {
            NftAsset nft = nftAssetMapper.selectById(order.getNftAssetId());
            if (nft != null) {
                nft.setOwnerAddress(buyerAddress);
                nft.setUpdatedAt(LocalDateTime.now());
                nftAssetMapper.updateById(nft);
            }
        }

        removeOrderCache(orderId);

        log.info("NFT purchased: orderId={}, buyer={}, price={} 积分, royalty={}, txHash={}",
                orderId, buyerAddress, toMatic(currentPrice), toMatic(royaltyAmount), txHash);

        auditLogService.log(null, AuditLog.ACTION_BUY, AuditLog.MODULE_MARKET,
                "购买NFT: orderId=" + orderId + ", price=" + toMatic(currentPrice) + "积分");
        return txHash;
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public String cancelOrder(String orderId, String sellerAddress) throws Exception {
        MarketOrder order = getOrderById(orderId);

        if (!sellerAddress.equalsIgnoreCase(order.getSellerAddress())) {
            throw new SecurityException("Only seller can cancel the order");
        }
        if (order.getStatus() != 1) {
            throw new IllegalStateException("Order is not active");
        }

        String txHash = generateTxHash(order.getId() * 100000L + System.currentTimeMillis() % 10000);

        order.setStatus(3);
        order.setTxHash(txHash);
        order.setUpdatedAt(LocalDateTime.now());

        marketOrderMapper.updateById(order);
        removeOrderCache(orderId);

        log.info("Order cancelled: orderId={}, txHash={}", orderId, txHash);

        auditLogService.log(null, AuditLog.ACTION_DELETE, AuditLog.MODULE_MARKET,
                "取消订单: orderId=" + orderId);
        return txHash;
    }

    /** 获取订单详情 */
    public MarketOrder getOrder(String orderId) {
        return getOrderById(orderId);
    }

    /** 获取订单列表 */
    public List<MarketOrder> getOrderList(String sellerAddress, Integer status, int page, int size) {
        LambdaQueryWrapper<MarketOrder> wrapper = new LambdaQueryWrapper<>();
        if (sellerAddress != null) wrapper.eq(MarketOrder::getSellerAddress, sellerAddress);
        if (status != null) wrapper.eq(MarketOrder::getStatus, status);
        wrapper.orderByDesc(MarketOrder::getCreatedAt);
        wrapper.last("LIMIT " + ((page - 1) * size) + ", " + size);

        List<MarketOrder> orders = marketOrderMapper.selectList(wrapper);
        for (MarketOrder order : orders) {
            if (order.getNftAssetId() != null) {
                NftAsset nft = nftAssetMapper.selectById(order.getNftAssetId());
                if (nft != null) {
                    order.setNftName(nft.getName());
                    order.setNftDescription(nft.getDescription());
                    order.setNftImageUrl(nft.getImageUrl());
                    order.setCategory(nft.getCategory());
                    order.setCreatorAddress(nft.getCreatorAddress());
                    order.setRoyaltyFee(nft.getRoyaltyFee());
                    order.setCurrentVersion(nft.getCurrentVersion());
                }
            }
        }
        return orders;
    }

    public List<MarketOrder> getUserHistory(String address, int page, int size) {
        LambdaQueryWrapper<MarketOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(MarketOrder::getSellerAddress, address)
                          .or().eq(MarketOrder::getBuyerAddress, address));
        wrapper.orderByDesc(MarketOrder::getUpdatedAt);
        wrapper.last("LIMIT " + ((page - 1) * size) + ", " + size);

        List<MarketOrder> orders = marketOrderMapper.selectList(wrapper);
        for (MarketOrder order : orders) {
            NftAsset nft = null;
            if (order.getNftAssetId() != null) {
                nft = nftAssetMapper.selectById(order.getNftAssetId());
            }
            if (nft == null && order.getTokenId() != null) {
                LambdaQueryWrapper<NftAsset> nftQuery = new LambdaQueryWrapper<>();
                nftQuery.eq(NftAsset::getTokenId, order.getTokenId()).last("LIMIT 1");
                nft = nftAssetMapper.selectOne(nftQuery);
            }
            if (nft != null) {
                order.setNftName(nft.getName());
                order.setNftDescription(nft.getDescription());
                order.setNftImageUrl(nft.getImageUrl());
                order.setCurrentVersion(nft.getCurrentVersion());
            }
        }
        return orders;
    }

    // ==================== 私有方法 ====================

    private MarketOrder getOrderById(String orderId) {
        MarketOrder order = getOrderFromCache(orderId);
        if (order == null) {
            // 先尝试按业务 orderId (UUID) 查
            LambdaQueryWrapper<MarketOrder> qw = new LambdaQueryWrapper<>();
            qw.eq(MarketOrder::getOrderId, orderId);
            order = marketOrderMapper.selectOne(qw);
        }
        if (order == null) {
            // 再尝试按数字主键 id 查
            try {
                Long numId = Long.parseLong(orderId);
                order = marketOrderMapper.selectById(numId);
            } catch (NumberFormatException ignored) {}
        }
        if (order == null) throw new IllegalArgumentException("Order not found: " + orderId);
        return order;
    }

    /**
     * 计算当前价格（固定价格直接返回，荷兰拍卖按时间线性递减）
     */
    private BigInteger getCurrentPrice(MarketOrder order) {
        if (order.getOrderType() == 1) {
            return order.getPrice();
        }
        if (order.getOrderType() == 2) {
            long startTimeSec = order.getCreatedAt()
                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000;
            long endTimeSec = order.getEndTime();
            long now = System.currentTimeMillis() / 1000;

            if (now >= endTimeSec) return order.getReservePrice();

            BigInteger startPrice = order.getStartPrice();
            BigInteger reservePrice = order.getReservePrice();
            long totalTime = endTimeSec - startTimeSec;
            long elapsed = now - startTimeSec;

            if (totalTime <= 0) return reservePrice;

            BigInteger discount = startPrice.subtract(reservePrice)
                    .multiply(BigInteger.valueOf(elapsed))
                    .divide(BigInteger.valueOf(totalTime));
            return startPrice.subtract(discount);
        }
        return order.getPrice();
    }

    private void enrichOrderWithNftInfo(MarketOrder order, Long nftAssetId) {
        try {
            NftAsset nft = nftAssetMapper.selectById(nftAssetId);
            if (nft != null) {
                order.setNftName(nft.getName());
                order.setNftDescription(nft.getDescription());
                order.setNftImageUrl(nft.getImageUrl());
                order.setCategory(nft.getCategory());
                order.setCreatorAddress(nft.getCreatorAddress());
                order.setCurrentVersion(nft.getCurrentVersion());
                marketOrderMapper.updateById(order);
            }
        } catch (Exception e) {
            log.warn("Failed to enrich order with NFT info", e);
        }
    }

    private void cacheOrder(MarketOrder order) {
        try {
            redisTemplate.opsForValue().set(ORDER_CACHE_KEY + order.getOrderId(), order, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Failed to cache order: {}", order.getOrderId());
        }
    }

    private MarketOrder getOrderFromCache(String orderId) {
        try {
            Object obj = redisTemplate.opsForValue().get(ORDER_CACHE_KEY + orderId);
            if (obj instanceof MarketOrder) return (MarketOrder) obj;
        } catch (Exception e) {
            log.warn("Failed to get order from cache: {}", orderId);
        }
        return null;
    }

    private void removeOrderCache(String orderId) {
        try {
            redisTemplate.delete(ORDER_CACHE_KEY + orderId);
        } catch (Exception e) {
            log.warn("Failed to remove order cache: {}", orderId);
        }
    }

    private String generateTxHash(long seed) {
        return "0x" + String.format("%064x", seed);
    }

    /** 通过钱包地址查找用户 */
    private User getUserByWalletAddress(String walletAddress) {
        LambdaQueryWrapper<Wallet> wq = new LambdaQueryWrapper<>();
        wq.eq(Wallet::getWalletAddress, walletAddress);
        Wallet wallet = walletMapper.selectOne(wq);
        if (wallet == null) return null;
        return userMapper.selectById(wallet.getUserId());
    }

    /** Wei → 可读积分（保留4位小数） */
    private String toMatic(BigInteger wei) {
        if (wei == null || wei.equals(BigInteger.ZERO)) return "0";
        java.math.BigDecimal d = new java.math.BigDecimal(wei)
                .divide(new java.math.BigDecimal("1000000000000000000"), 4, java.math.RoundingMode.DOWN);
        return d.stripTrailingZeros().toPlainString();
    }
}
