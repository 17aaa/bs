package com.zhm.springboot.userservice.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.wallet.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户钱包 Mapper 接口
 */
@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
}