package com.zhm.springboot.userservice.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.service.AdminFanTokenService;
import com.zhm.springboot.userservice.admin.vo.AdminFanTokenVO;
import com.zhm.springboot.userservice.fantoken.entity.FanToken;
import com.zhm.springboot.userservice.fantoken.mapper.FanTokenMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端粉丝代币服务实现
 */
@Service
@Slf4j
public class AdminFanTokenServiceImpl implements AdminFanTokenService {

    @Autowired
    private FanTokenMapper fanTokenMapper;

    @Override
    public Page<AdminFanTokenVO> getFanTokenList(Page<AdminFanTokenVO> page, String keyword, Integer status) {
        log.info("获取粉丝代币列表: keyword={}, status={}", keyword, status);

        Page<FanToken> tokenPage = new Page<>(page.getCurrent(), page.getSize());
        QueryWrapper<FanToken> wrapper = new QueryWrapper<>();

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("name", keyword)
                    .or().like("symbol", keyword)
                    .or().like("creator_address", keyword));
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq("status", status);
        }

        // 排序
        wrapper.orderByDesc("created_at");

        fanTokenMapper.selectPage(tokenPage, wrapper);

        // 转换为 VO
        List<AdminFanTokenVO> voList = tokenPage.getRecords().stream().map(token -> {
            AdminFanTokenVO vo = new AdminFanTokenVO();
            BeanUtils.copyProperties(token, vo);
            vo.setCreatorUsername("未知"); // 简化处理，实际需要关联查询
            return vo;
        }).collect(Collectors.toList());

        page.setRecords(voList);
        page.setTotal(tokenPage.getTotal());

        return page;
    }

    @Override
    public FanToken getFanTokenDetail(Long tokenId) {
        log.info("获取粉丝代币详情: tokenId={}", tokenId);
        return fanTokenMapper.selectById(tokenId);
    }

    @Override
    public boolean activatePublicSale(Long tokenId) {
        log.info("激活公募: tokenId={}", tokenId);

        FanToken token = fanTokenMapper.selectById(tokenId);
        if (token == null) {
            return false;
        }

        token.setPublicSaleActive(true);
        token.setStatus(1); // 正常状态
        return fanTokenMapper.updateById(token) > 0;
    }

    @Override
    public boolean pausePublicSale(Long tokenId) {
        log.info("暂停公募: tokenId={}", tokenId);

        FanToken token = fanTokenMapper.selectById(tokenId);
        if (token == null) {
            return false;
        }

        token.setPublicSaleActive(false);
        token.setStatus(2); // 暂停状态
        return fanTokenMapper.updateById(token) > 0;
    }

    @Override
    public boolean endPublicSale(Long tokenId) {
        log.info("结束公募: tokenId={}", tokenId);

        FanToken token = fanTokenMapper.selectById(tokenId);
        if (token == null) {
            return false;
        }

        token.setPublicSaleActive(false);
        token.setStatus(3); // 结束状态
        return fanTokenMapper.updateById(token) > 0;
    }
}