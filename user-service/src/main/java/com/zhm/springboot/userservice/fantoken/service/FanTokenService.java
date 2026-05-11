package com.zhm.springboot.userservice.fantoken.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhm.springboot.userservice.audit.entity.AuditLog;
import com.zhm.springboot.userservice.audit.service.AuditLogService;
import com.zhm.springboot.userservice.fantoken.entity.FanToken;
import com.zhm.springboot.userservice.fantoken.entity.StakeRecord;
import com.zhm.springboot.userservice.fantoken.mapper.FanTokenMapper;
import com.zhm.springboot.userservice.fantoken.mapper.StakeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台积分服务（FanToken）
 * 实现创作者发行平台积分、用户参与公募、质押挖矿等激励机制
 * 积分在平台内流通，通过数据库记录余额，无需链上 ERC-20 合约调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FanTokenService {

    private final FanTokenMapper fanTokenMapper;
    private final StakeRecordMapper stakeRecordMapper;
    private final AuditLogService auditLogService;

    /**
     * 创建粉丝积分项目
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createFanToken(Long projectId, String creatorAddress,
                                String name, String symbol, BigInteger totalSupply) {
        LambdaQueryWrapper<FanToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FanToken::getProjectId, projectId);
        if (fanTokenMapper.selectOne(wrapper) != null) {
            throw new IllegalStateException("Fan token already exists for project: " + projectId);
        }

        FanToken fanToken = new FanToken();
        fanToken.setTokenAddress(generateTokenAddress(projectId));
        fanToken.setProjectId(projectId);
        fanToken.setCreatorAddress(creatorAddress);
        fanToken.setName(name);
        fanToken.setSymbol(symbol);
        fanToken.setTotalSupply(totalSupply);
        // 40% 用于公募，60% 留给创作者/质押奖励
        fanToken.setPublicSaleRemaining(totalSupply.multiply(BigInteger.valueOf(40)).divide(BigInteger.valueOf(100)));
        fanToken.setPublicSalePrice(BigInteger.ZERO);
        fanToken.setPublicSaleActive(false);
        fanToken.setStatus(1);
        fanToken.setCreatedAt(LocalDateTime.now());

        fanTokenMapper.insert(fanToken);

        // 初始化创作者质押记录（余额为总供应量 60%）
        StakeRecord stakeRecord = new StakeRecord();
        stakeRecord.setUserAddress(creatorAddress);
        stakeRecord.setTokenAddress(fanToken.getTokenAddress());
        stakeRecord.setStakedAmount(BigInteger.ZERO);
        stakeRecord.setRewardEarned(BigInteger.ZERO);
        // 创作者持有份额（总量 60%）
        stakeRecord.setBalance(totalSupply.multiply(BigInteger.valueOf(60)).divide(BigInteger.valueOf(100)));
        stakeRecord.setLastUpdateTime(System.currentTimeMillis() / 1000);
        stakeRecord.setCreatedAt(LocalDateTime.now());

        stakeRecordMapper.insert(stakeRecord);

        log.info("Created fan token: {} ({}) for project {}", name, symbol, projectId);

        auditLogService.log(null, AuditLog.ACTION_CREATE, AuditLog.MODULE_NFT,
                "发行粉丝代币: " + name + " (" + symbol + ")");
        return fanToken.getId();
    }

    /**
     * 配置公募
     */
    @Transactional(rollbackFor = Exception.class)
    public void configurePublicSale(String tokenAddress, BigInteger price,
                                     Long startTime, Long endTime) {
        FanToken fanToken = getFanTokenByAddress(tokenAddress);

        if (startTime >= endTime) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (price.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        fanToken.setPublicSalePrice(price);
        fanToken.setPublicSaleActive(true);
        fanTokenMapper.updateById(fanToken);

        log.info("Configured public sale for token: {}, price={}", tokenAddress, price);
    }

    /**
     * 参与公募（购买积分）
     */
    @Transactional(rollbackFor = Exception.class)
    public void participateInSale(String tokenAddress, String buyerAddress,
                                   BigInteger amount, BigInteger paymentAmount) {
        FanToken fanToken = getFanTokenByAddress(tokenAddress);

        if (!fanToken.getPublicSaleActive()) {
            throw new IllegalStateException("Public sale is not active");
        }

        BigInteger remaining = fanToken.getPublicSaleRemaining();
        if (amount.compareTo(remaining) > 0) {
            throw new IllegalStateException("Insufficient tokens for sale");
        }

        // 验证支付金额
        BigInteger requiredAmount = amount.multiply(fanToken.getPublicSalePrice())
                .divide(BigInteger.valueOf(1_000_000_000_000_000_000L));
        if (paymentAmount.compareTo(requiredAmount) < 0) {
            throw new IllegalStateException("Insufficient payment");
        }

        // 扣减公募剩余量
        fanToken.setPublicSaleRemaining(remaining.subtract(amount));
        fanTokenMapper.updateById(fanToken);

        // 增加买家余额（平台数据库记录）
        StakeRecord buyerRecord = getOrCreateStakeRecord(buyerAddress, tokenAddress);
        buyerRecord.setBalance(buyerRecord.getBalance().add(amount));
        stakeRecordMapper.updateById(buyerRecord);

        log.info("Participated in sale: {} tokens to {}", amount, buyerAddress);

        auditLogService.log(null, AuditLog.ACTION_BUY, AuditLog.MODULE_MARKET,
                "参与代币公募: " + amount + " tokens, token=" + tokenAddress);
    }

    /**
     * 质押积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void stake(String userAddress, String tokenAddress, BigInteger amount) {
        StakeRecord record = getOrCreateStakeRecord(userAddress, tokenAddress);

        if (record.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance to stake");
        }

        updateReward(record);

        record.setBalance(record.getBalance().subtract(amount));
        record.setStakedAmount(record.getStakedAmount().add(amount));
        record.setLastUpdateTime(System.currentTimeMillis() / 1000);
        stakeRecordMapper.updateById(record);

        log.info("Staked {} tokens for user {}", amount, userAddress);

        auditLogService.log(null, AuditLog.ACTION_TRANSFER, AuditLog.MODULE_NFT,
                "质押代币: " + amount + ", token=" + tokenAddress);
    }

    /**
     * 解除质押
     */
    @Transactional(rollbackFor = Exception.class)
    public void unstake(String userAddress, String tokenAddress, BigInteger amount) {
        StakeRecord record = getOrCreateStakeRecord(userAddress, tokenAddress);

        if (record.getStakedAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient staked amount");
        }

        updateReward(record);

        record.setStakedAmount(record.getStakedAmount().subtract(amount));
        record.setBalance(record.getBalance().add(amount));
        record.setLastUpdateTime(System.currentTimeMillis() / 1000);
        stakeRecordMapper.updateById(record);

        log.info("Unstaked {} tokens for user {}", amount, userAddress);
    }

    /**
     * 领取质押奖励
     */
    @Transactional(rollbackFor = Exception.class)
    public BigInteger claimReward(String userAddress, String tokenAddress) {
        StakeRecord record = getOrCreateStakeRecord(userAddress, tokenAddress);

        updateReward(record);

        BigInteger reward = record.getRewardEarned();
        if (reward.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalStateException("No reward to claim");
        }

        // 奖励转为余额
        record.setRewardEarned(BigInteger.ZERO);
        record.setBalance(record.getBalance().add(reward));
        stakeRecordMapper.updateById(record);

        log.info("Claimed {} reward for user {}", reward, userAddress);
        return reward;
    }

    /** 获取质押记录 */
    public StakeRecord getStakeRecord(String userAddress, String tokenAddress) {
        return getOrCreateStakeRecord(userAddress, tokenAddress);
    }

    /** 获取粉丝代币信息 */
    public FanToken getFanTokenByAddress(String tokenAddress) {
        LambdaQueryWrapper<FanToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FanToken::getTokenAddress, tokenAddress);
        FanToken fanToken = fanTokenMapper.selectOne(wrapper);
        if (fanToken == null) throw new IllegalArgumentException("Fan token not found: " + tokenAddress);
        return fanToken;
    }

    /** 获取创作者的所有粉丝代币 */
    public List<FanToken> getFanTokensByCreator(String creatorAddress) {
        LambdaQueryWrapper<FanToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FanToken::getCreatorAddress, creatorAddress);
        wrapper.orderByDesc(FanToken::getCreatedAt);
        return fanTokenMapper.selectList(wrapper);
    }

    // ==================== 私有方法 ====================

    private StakeRecord getOrCreateStakeRecord(String userAddress, String tokenAddress) {
        LambdaQueryWrapper<StakeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StakeRecord::getUserAddress, userAddress);
        wrapper.eq(StakeRecord::getTokenAddress, tokenAddress);

        StakeRecord record = stakeRecordMapper.selectOne(wrapper);
        if (record == null) {
            record = new StakeRecord();
            record.setUserAddress(userAddress);
            record.setTokenAddress(tokenAddress);
            record.setStakedAmount(BigInteger.ZERO);
            record.setRewardEarned(BigInteger.ZERO);
            record.setBalance(BigInteger.ZERO);
            record.setLastUpdateTime(System.currentTimeMillis() / 1000);
            record.setCreatedAt(LocalDateTime.now());
            stakeRecordMapper.insert(record);
        }
        return record;
    }

    /**
     * 计算并累积质押奖励（年化约 3.15%，每秒 0.0001%）
     */
    private void updateReward(StakeRecord record) {
        long now = System.currentTimeMillis() / 1000;
        long elapsed = now - record.getLastUpdateTime();

        if (elapsed > 0 && record.getStakedAmount().compareTo(BigInteger.ZERO) > 0) {
            // 每秒奖励 = 质押量 × 时间 / 1_000_000
            BigInteger reward = record.getStakedAmount()
                    .multiply(BigInteger.valueOf(elapsed))
                    .divide(BigInteger.valueOf(1_000_000));
            record.setRewardEarned(record.getRewardEarned().add(reward));
        }
        record.setLastUpdateTime(now);
    }

    /**
     * 生成确定性的代币地址（基于项目 ID，格式合法的以太坊地址）
     */
    private String generateTokenAddress(Long projectId) {
        return "0x" + String.format("%040x", projectId);
    }
}
