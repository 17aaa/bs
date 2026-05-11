package com.zhm.springboot.userservice.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 市场订单实体类
 */
@Data
@TableName("market_orders")
public class MarketOrder {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单 ID（业务唯一标识）
     */
    private String orderId;

    /**
     * 链上 Sale ID
     */
    private Long saleId;

    /**
     * 卖家地址
     */
    private String sellerAddress;

    /**
     * 买家地址
     */
    private String buyerAddress;

    /**
     * NFT 合约地址
     */
    private String nftContract;

    /**
     * Token ID
     */
    private Long tokenId;

    /**
     * 订单类型：1-固定价格 2-荷兰拍卖 3-报价
     */
    private Integer orderType;

    /**
     * 状态：1-活跃 2-已售 3-取消 4-过期
     */
    private Integer status;

    /**
     * 价格（Wei 单位）
     */
    private BigInteger price;

    /**
     * 拍卖起始价
     */
    private BigInteger startPrice;

    /**
     * 拍卖保留价
     */
    private BigInteger reservePrice;

    /**
     * 支付代币地址（0x0 = ETH/MATIC）
     */
    private String paymentToken;

    /**
     * 结束时间（时间戳）
     */
    private Long endTime;

    /**
     * 版税比例（万分比）
     */
    private Integer royaltyFee;

    /**
     * 版税接收地址
     */
    private String royaltyRecipient;

    /**
     * 最终成交价
     */
    private BigInteger finalPrice;

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * NFT 资产 ID（关联本地 NFT 记录）
     */
    private Long nftAssetId;

    /**
     * NFT 名称（冗余字段，用于展示）
     */
    @TableField(exist = false)
    private String nftName;

    /**
     * NFT 描述（冗余字段，用于展示）
     */
    @TableField(exist = false)
    private String nftDescription;

    /**
     * NFT 图片 URL（冗余字段，用于展示）
     */
    @TableField(exist = false)
    private String nftImageUrl;

    /**
     * NFT 分类（冗余字段，用于展示）
     */
    @TableField(exist = false)
    private String category;

    /**
     * NFT 创作者地址（冗余字段，用于展示）
     */
    @TableField(exist = false)
    private String creatorAddress;

    /**
     * NFT 当前版本号（冗余字段，用于展示）
     */
    @TableField(exist = false)
    private Integer currentVersion;
}