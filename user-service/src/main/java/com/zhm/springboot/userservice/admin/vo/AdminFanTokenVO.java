package com.zhm.springboot.userservice.admin.vo;

import lombok.Data;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 管理端粉丝代币 VO
 */
@Data
public class AdminFanTokenVO {
    /**
     * 代币 ID
     */
    private Long id;

    /**
     * 代币地址
     */
    private String tokenAddress;

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
     * 创作者地址
     */
    private String creatorAddress;

    /**
     * 创作者用户名
     */
    private String creatorUsername;

    /**
     * 状态：1-正常 2-暂停 3-结束
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}