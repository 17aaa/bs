package com.zhm.springboot.userservice.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.vo.AdminFanTokenVO;
import com.zhm.springboot.userservice.fantoken.entity.FanToken;

/**
 * 管理端粉丝代币服务接口
 */
public interface AdminFanTokenService {

    /**
     * 获取粉丝代币列表
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param status 状态筛选
     */
    Page<AdminFanTokenVO> getFanTokenList(Page<AdminFanTokenVO> page, String keyword, Integer status);

    /**
     * 获取粉丝代币详情
     * @param tokenId 代币 ID
     */
    FanToken getFanTokenDetail(Long tokenId);

    /**
     * 激活公募
     * @param tokenId 代币 ID
     */
    boolean activatePublicSale(Long tokenId);

    /**
     * 暂停公募
     * @param tokenId 代币 ID
     */
    boolean pausePublicSale(Long tokenId);

    /**
     * 结束公募
     * @param tokenId 代币 ID
     */
    boolean endPublicSale(Long tokenId);
}