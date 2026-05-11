package com.zhm.springboot.userservice.nft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.nft.entity.NftVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * NFT 版本历史 Mapper 接口
 */
@Mapper
public interface NftVersionMapper extends BaseMapper<NftVersion> {
}