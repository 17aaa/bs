package com.zhm.springboot.userservice.fantoken.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 粉丝代币实体类
 */
@Data
@TableName("fan_tokens")
public class FanToken {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 代币地址
     */
    private String tokenAddress;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 创作者地址
     */
    private String creatorAddress;

    /**
     * 代币名称
     */
    private String name;

    /**
     * 代币符号
     */
    private String symbol;

    /**
     * 总供应量
     */
    private BigInteger totalSupply;

    /**
     * 公募剩余量
     */
    private BigInteger publicSaleRemaining;

    /**
     * 公募价格（Wei）
     */
    private BigInteger publicSalePrice;

    /**
     * 公募是否激活
     */
    private Boolean publicSaleActive;

    /**
     * 粉丝代币合约地址
     */
    private String contractAddress;

    /**
     * 状态：1-正常 2-暂停 3-结束
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}