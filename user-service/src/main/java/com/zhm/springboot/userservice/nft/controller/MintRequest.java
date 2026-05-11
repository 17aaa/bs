package com.zhm.springboot.userservice.nft.controller;

import lombok.Data;

/**
 * NFT 铸造请求 DTO
 */
@Data
public class MintRequest {
    private String creatorAddress;
    private String name;
    private String description;
    private String category;
    private String metadataUrl;
    private Integer royaltyFee;
}