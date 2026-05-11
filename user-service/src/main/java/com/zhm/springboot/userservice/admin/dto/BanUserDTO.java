package com.zhm.springboot.userservice.admin.dto;

import lombok.Data;

/**
 * 封禁用户 DTO
 */
@Data
public class BanUserDTO {
    /**
     * 封禁原因
     */
    private String reason;
}