package com.zhm.springboot.userservice.nft.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建铸造任务请求 DTO
 */
@Data
public class MintTaskCreateDTO {

    @NotBlank(message = "创作者地址不能为空")
    @Pattern(regexp = "^0x[0-9a-fA-F]{40}$", message = "创作者地址格式不正确（需为0x开头的42位十六进制地址）")
    private String creatorAddress;

    @NotBlank(message = "作品名称不能为空")
    @Size(max = 200, message = "作品名称不能超过200字")
    private String name;

    @Size(max = 2000, message = "作品描述不能超过2000字")
    private String description;

    @Size(max = 50, message = "分类名称不能超过50字")
    private String category;

    @NotBlank(message = "图片URL不能为空")
    @Size(max = 2048, message = "图片URL长度不能超过2048字符")
    private String imageUrl;

    @Min(value = 0, message = "版税比例不能为负数")
    @Max(value = 1000, message = "版税比例不能超过1000（即10%）")
    private Integer royaltyFee;
}
