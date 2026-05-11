package com.zhm.springboot.userservice.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.admin.dto.AdminUserUpdateDTO;
import com.zhm.springboot.userservice.admin.dto.BanUserDTO;
import com.zhm.springboot.userservice.admin.service.AdminUserService;
import com.zhm.springboot.userservice.admin.vo.AdminUserDetailVO;
import com.zhm.springboot.userservice.admin.vo.AdminUserVO;
import com.zhm.springboot.userservice.client.PermissionClient;
import com.zhm.springboot.userservice.entity.User;
import com.zhm.springboot.userservice.fantoken.mapper.FanTokenMapper;
import com.zhm.springboot.userservice.mapper.UserMapper;
import com.zhm.springboot.userservice.market.mapper.MarketOrderMapper;
import com.zhm.springboot.userservice.nft.mapper.NftAssetMapper;
import com.zhm.springboot.userservice.util.PasswordUtil;
import com.zhm.springboot.userservice.wallet.entity.Wallet;
import com.zhm.springboot.userservice.wallet.mapper.WalletMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端用户服务实现
 */
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private NftAssetMapper nftAssetMapper;

    @Autowired
    private MarketOrderMapper marketOrderMapper;

    @Autowired
    private FanTokenMapper fanTokenMapper;

    @Autowired
    private PermissionClient permissionClient;

    @Override
    public Page<AdminUserVO> getUserList(Page<AdminUserVO> page, String keyword, Integer status, String role) {
        log.info("获取用户列表: keyword={}, status={}, role={}", keyword, status, role);

        // 先查询用户 ID 列表
        Page<User> userPage = new Page<>(page.getCurrent(), page.getSize());
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("email", keyword)
                    .or().like("phone", keyword)
                    .or().like("school", keyword));
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq("status", status);
        }

        // 排序
        wrapper.orderByDesc("gmt_create");

        userMapper.selectPage(userPage, wrapper);

        // 转换为 VO
        List<AdminUserVO> voList = userPage.getRecords().stream().map(user -> {
            AdminUserVO vo = new AdminUserVO();
            BeanUtils.copyProperties(user, vo);

            // 获取角色
            try {
                vo.setRole(permissionClient.getUserRole(user.getUserId()));
            } catch (Exception e) {
                log.warn("获取用户角色失败: userId={}", user.getUserId());
                vo.setRole("user");
            }

            // 获取钱包地址
            QueryWrapper<Wallet> walletWrapper = new QueryWrapper<>();
            walletWrapper.eq("user_id", user.getUserId());
            Wallet wallet = walletMapper.selectOne(walletWrapper);
            if (wallet != null) {
                vo.setWalletAddress(wallet.getWalletAddress());
            }

            // 获取 NFT 数量
            vo.setNftCount(0); // 简化处理，实际需要根据 owner_address 关联查询

            // 获取交易次数
            vo.setTradeCount(0); // 简化处理

            return vo;
        }).collect(Collectors.toList());

        page.setRecords(voList);
        page.setTotal(userPage.getTotal());

        return page;
    }

    @Override
    public AdminUserDetailVO getUserDetail(Long userId) {
        log.info("获取用户详情: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        AdminUserDetailVO vo = new AdminUserDetailVO();
        BeanUtils.copyProperties(user, vo);

        // 获取角色
        try {
            vo.setRole(permissionClient.getUserRole(userId));
        } catch (Exception e) {
            log.warn("获取用户角色失败: userId={}", userId);
            vo.setRole("user");
        }

        // 获取钱包信息
        QueryWrapper<Wallet> walletWrapper = new QueryWrapper<>();
        walletWrapper.eq("user_id", userId);
        Wallet wallet = walletMapper.selectOne(walletWrapper);
        if (wallet != null) {
            vo.setWalletAddress(wallet.getWalletAddress());
            if (wallet.getCreatedAt() != null) {
                vo.setWalletCreatedAt(Timestamp.valueOf(wallet.getCreatedAt()));
            }
        }

        // NFT 统计 - 简化处理
        vo.setCreatedNftCount(0);
        vo.setOwnedNftCount(0);

        // 交易统计 - 简化处理
        vo.setSellerTradeCount(0);
        vo.setBuyerTradeCount(0);
        vo.setTotalTradeVolume(BigInteger.ZERO);

        return vo;
    }

    @Override
    public boolean updateUser(Long userId, AdminUserUpdateDTO dto) {
        log.info("更新用户信息: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getSchool() != null) {
            user.setSchool(dto.getSchool());
        }
        if (dto.getMajor() != null) {
            user.setMajor(dto.getMajor());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }

        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码: userId={}", userId);

        if (!PasswordUtil.isPasswordValid(newPassword)) {
            throw new IllegalArgumentException(PasswordUtil.getPasswordRequirements());
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        user.setPassword(PasswordUtil.encryptPassword(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean banUser(Long userId, BanUserDTO dto) {
        log.info("封禁用户: userId={}, reason={}", userId, dto.getReason());

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        user.setStatus(2); // 禁用状态
        user.setBanReason(dto.getReason());
        user.setBannedAt(new Timestamp(System.currentTimeMillis()));

        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean unbanUser(Long userId) {
        log.info("解封用户: userId={}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        user.setStatus(1); // 正常状态
        user.setBanReason(null);
        user.setBannedAt(null);

        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean upgradeToAdmin(Long userId) {
        log.info("升级用户为管理员: userId={}", userId);

        try {
            permissionClient.upgradeToAdmin(userId);
            return true;
        } catch (Exception e) {
            log.error("升级管理员失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean downgradeToUser(Long userId) {
        log.info("降级管理员为普通用户: userId={}", userId);

        try {
            permissionClient.downgradeToUser(userId);
            return true;
        } catch (Exception e) {
            log.error("降级用户失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }
}