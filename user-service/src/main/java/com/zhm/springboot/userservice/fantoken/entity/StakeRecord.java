package com.zhm.springboot.userservice.fantoken.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 质押记录实体类
 */
@Data
@TableName("stake_records")
public class StakeRecord {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户地址
     */
    private String userAddress;

    /**
     * 代币地址
     */
    private String tokenAddress;

    /**
     * 质押数量
     */
    private BigInteger stakedAmount;

    /**
     * 累计奖励
     */
    private BigInteger rewardEarned;

    /**
     * 可用余额（未质押部分）
     */
    private BigInteger balance;

    /**
     * 最后更新时间（时间戳）
     */
    private Long lastUpdateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}