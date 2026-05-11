package com.zhm.springboot.userservice.nft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import org.apache.ibatis.annotations.Mapper;

/**
 * NFT 资产 Mapper 接口
 */
@Mapper
public interface NftAssetMapper extends BaseMapper<NftAsset> {
}