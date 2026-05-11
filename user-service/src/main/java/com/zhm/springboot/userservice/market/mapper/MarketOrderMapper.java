package com.zhm.springboot.userservice.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.market.entity.MarketOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 市场订单 Mapper 接口
 */
@Mapper
public interface MarketOrderMapper extends BaseMapper<MarketOrder> {
}