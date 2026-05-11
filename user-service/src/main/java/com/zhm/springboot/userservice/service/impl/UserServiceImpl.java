package com.zhm.springboot.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.client.PermissionClient;
import com.zhm.springboot.userservice.entity.User;
import com.zhm.springboot.userservice.mapper.UserMapper;
import com.zhm.springboot.userservice.service.UserService;
import com.zhm.springboot.userservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zhm.springboot.userservice.util.PasswordUtil;

/**
  日志打印由ai检索并自动化生成,加密和ai要的，一些处理也使用了补全，不一一列举了
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PermissionClient permissionClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(User user) {
        logger.info("开始注册用户: username={}", user.getUsername());
        if (!PasswordUtil.isPasswordValid(user.getPassword())) {
            throw new IllegalArgumentException(PasswordUtil.getPasswordRequirements());
        }

        // 加密
        user.setPassword(PasswordUtil.encryptPassword(user.getPassword()));

        // 注册赠送 100 积分（100 * 10^18 Wei）
        user.setPlatformBalance(new java.math.BigInteger("100000000000000000000"));

        userMapper.insert(user);
        logger.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());
        return user;
    }

    @Override
    public String login(String username, String password) {
        logger.debug("登录尝试: username={}", username);
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));

        if (user != null) {

            String storedPassword = user.getPassword();

            // 如果是明文密码，迁移为加密格式
            if (!PasswordUtil.isEncrypted(storedPassword)) {
                String encrypted = PasswordUtil.migratePassword(password, storedPassword);
                user.setPassword(encrypted);
                userMapper.updateById(user);
            }

            if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                logger.info("用户登录成功: userId={}, username={}", user.getUserId(), username);
                return jwtUtil.createToken(user.getUserId());
            }
        }
        logger.warn("登录失败: username={} (用户不存在或密码错误)", username);
        return null;
    }

    @Override
    public Page<User> getUserList(Page<User> page, Long currentUserId) {
        logger.debug("获取用户列表: currentUserId={}, page={}", currentUserId, page.getCurrent());
        String role = permissionClient.getUserRole(currentUserId);
        logger.debug("当前用户角色: userId={}, role={}", currentUserId, role);

        //过滤找ai学习的，超管可以查看所有用户，管理员只能查看普通用户，普通用户只能查看自身账户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if ("admin".equals(role)) {
            wrapper.ne("user_id", 1L);
            logger.debug("管理员视图: 过滤超管账户");
        } else if (!"super_admin".equals(role)) {
            wrapper.eq("user_id", currentUserId);
            logger.debug("普通用户视图: 仅显示自身账户");
        }
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public User getUserInfo(Long userId, Long currentUserId) {
        logger.debug("查询用户信息: targetUserId={}, currentUserId={}", userId, currentUserId);

        // 用户查询自己的信息不需要权限验证
        if (userId.equals(currentUserId)) {
            logger.info("用户查询自身信息: userId={}", userId);
            return userMapper.selectById(userId);
        }

        // 查询他人信息时需要权限验证
        String role = "user";
        try {
            role = permissionClient.getUserRole(currentUserId);
        } catch (Exception e) {
            logger.warn("获取用户角色失败，使用默认角色: error={}", e.getMessage());
        }

        if ("super_admin".equals(role) ||
                ("admin".equals(role) && !"super_admin".equals(permissionClient.getUserRole(userId)))) {
            logger.info("授权访问用户信息: operator={}, target={}", currentUserId, userId);
            return userMapper.selectById(userId);
        }

        logger.warn("无权限访问: operator={} 尝试访问 target={}", currentUserId, userId);
        return null;
    }

    @Override
    public boolean updateUserInfo(Long userId, Long currentUserId, String newEmail, String newPhone) {
        String role = permissionClient.getUserRole(currentUserId);
        User user = new User();
        user.setUserId(userId);
        logger.debug("更新用户信息请求: targetUserId={}, operator={}", userId, currentUserId);
        if (userId.equals(currentUserId)) {
            logger.info("用户更新自身信息: userId={}", userId);
            user.setEmail(newEmail);
            user.setPhone(newPhone);
            return userMapper.updateById(user) > 0;
        }

        if ("super_admin".equals(role) ||
                ("admin".equals(role) && !"super_admin".equals(permissionClient.getUserRole(userId)))) {
            logger.info("授权更新用户信息: operator={}, target={}", currentUserId, userId);
            user.setEmail(newEmail);
            user.setPhone(newPhone);
            return userMapper.updateById(user) > 0;
        }

        logger.warn("更新操作被拒绝: operator={} 无权限修改 target={}", currentUserId, userId);
        return false;
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword, Long currentUserId) {
        logger.debug("密码重置请求: targetUserId={}, operator={}", userId, currentUserId);
        String role = permissionClient.getUserRole(currentUserId);
        if (!PasswordUtil.isPasswordValid(newPassword)) {
            throw new IllegalArgumentException(PasswordUtil.getPasswordRequirements());
        }

        String encryptedPassword = PasswordUtil.encryptPassword(newPassword);

        if (userId.equals(currentUserId)) {
            logger.info("用户重置自身密码: userId={}", userId);
            User user = userMapper.selectById(userId);
            user.setUserId(userId);
            user.setPassword(encryptedPassword);
            return userMapper.updateById(user) > 0;
        }

        if ("super_admin".equals(role) ||
                ("admin".equals(role) && !"super_admin".equals(permissionClient.getUserRole(userId)))) {
            logger.info("授权密码重置: operator={}, target={}", currentUserId, userId);
            User user = userMapper.selectById(userId);
            user.setUserId(userId);
            user.setPassword(encryptedPassword);
            return userMapper.updateById(user) > 0;
        }

        logger.warn("密码重置被拒绝: operator={} 无权限操作 target={}", currentUserId, userId);
        return false;
    }

    @Override
    public boolean updateById(User user) {
        logger.info("更新用户: userId={}", user.getUserId());
        return userMapper.updateById(user) > 0;
    }
}