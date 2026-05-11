package com.zhm.springboot.userservice.fantoken.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.fantoken.entity.FanToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 粉丝代币 Mapper 接口
 */
@Mapper
public interface FanTokenMapper extends BaseMapper<FanToken> {
}