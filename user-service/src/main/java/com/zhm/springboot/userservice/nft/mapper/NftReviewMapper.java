package com.zhm.springboot.userservice.nft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.nft.entity.NftReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * NFT 审核记录 Mapper
 */
@Mapper
public interface NftReviewMapper extends BaseMapper<NftReview> {
}