package com.zhm.springboot.userservice.fantoken.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.fantoken.entity.StakeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质押记录 Mapper 接口
 */
@Mapper
public interface StakeRecordMapper extends BaseMapper<StakeRecord> {
}