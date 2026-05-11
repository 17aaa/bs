package com.zhm.springboot.userservice.admin.vo;

import lombok.Data;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 管理端订单列表 VO
 */
@Data
public class AdminOrderVO {
    /**
     * 订单 ID
     */
    private Long id;

    /**
     * 订单 ID（业务唯一标识）
     */
    private String orderId;

    /**
     * 卖家地址
     */
    private String sellerAddress;

    /**
     * 买家地址
     */
    private String buyerAddress;

    /**
     * NFT 名称
     */
    private String nftName;

    /**
     * NFT 图片 URL
     */
    private String nftImageUrl;

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
     * 卖家用户名
     */
    private String sellerUsername;

    /**
     * 买家用户名
     */
    private String buyerUsername;
}