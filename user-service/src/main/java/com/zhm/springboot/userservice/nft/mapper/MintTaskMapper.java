package com.zhm.springboot.userservice.nft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.nft.entity.MintTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 铸造任务 Mapper 接口
 */
@Mapper
public interface MintTaskMapper extends BaseMapper<MintTask> {
}