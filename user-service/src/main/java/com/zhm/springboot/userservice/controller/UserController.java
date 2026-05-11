package com.zhm.springboot.userservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.client.PermissionClient;
import com.zhm.springboot.userservice.entity.User;
import com.zhm.springboot.userservice.mq.OperationLogProducer;
import com.zhm.springboot.userservice.service.UserService;
import com.zhm.springboot.userservice.util.JwtUtil;
import com.zhm.springboot.userservice.wallet.entity.Wallet;
import com.zhm.springboot.userservice.wallet.service.WalletService;
import com.zhm.springboot.userservice.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
//日志打印由 ai 检索并自动化生成，一些处理也使用了补全
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionClient permissionClient;

    @Autowired
    private OperationLogProducer operationLogProducer;

    @Autowired
    private WalletService walletService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody User user, HttpServletRequest request) {
        log.info("收到注册请求：username={}, IP={}", user.getUsername(), request.getRemoteAddr());

        try {
            User registeredUser = userService.register(user);

            // 绑定默认角色（如果 permission-service 不可用则忽略）
            try {
                permissionClient.bindDefaultRole(registeredUser.getUserId());
            } catch (Exception e) {
                log.warn("绑定默认角色失败，permission-service 可能未启动：{}", e.getMessage());
            }

            operationLogProducer.sendRegisterLog(registeredUser, request.getRemoteAddr());

            // 自动为用户创建钱包
            String walletPassword = "wallet_" + registeredUser.getUserId() + "_" + System.currentTimeMillis();
            String walletAddress = null;
            try {
                var wallet = walletService.createWalletForUser(registeredUser.getUserId(), walletPassword);
                walletAddress = wallet.getWalletAddress();
                log.info("自动创建钱包成功：userId={}, address={}", registeredUser.getUserId(), walletAddress);
            } catch (Exception e) {
                log.warn("自动创建钱包失败：userId={}, error={}", registeredUser.getUserId(), e.getMessage());
            }

            // 返回用户信息和钱包信息
            Map<String, Object> result = new HashMap<>();
            result.put("userId", registeredUser.getUserId());
            result.put("username", registeredUser.getUsername());
            result.put("walletConnected", walletAddress != null);
            result.put("walletAddress", walletAddress);

            // 生成 JWT token，自动登录
            String token = jwtUtil.createToken(registeredUser.getUserId());
            String refreshToken = jwtUtil.createRefreshToken(registeredUser.getUserId());
            redisTemplate.opsForValue().set(
                "refresh:" + registeredUser.getUserId(), refreshToken,
                jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);
            result.put("token", token);
            result.put("refreshToken", refreshToken);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("注册失败：username={}, error={}", user.getUsername(), e.getMessage());
            return ApiResponse.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 从Authorization header中提取token（去掉Bearer前缀）
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    /**
     * 用户注销接口
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.parseToken(extractToken(token));
            log.info("收到注销请求：userId={}, IP={}", userId, request.getRemoteAddr());

            operationLogProducer.sendOperationLog(userId, "logout", request.getRemoteAddr(),
                    "用户注销");

            // 删除 Redis 中的 Refresh Token，使其立即失效
            redisTemplate.delete("refresh:" + userId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("注销失败：error={}", e.getMessage());
            return ApiResponse.error("注销失败");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getUserInfo(
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        try {
            // 去掉 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Long userId = jwtUtil.parseToken(extractToken(token));
            if (userId == null) return ApiResponse.error("获取用户信息失败");
            log.info("获取用户信息：userId={}", userId);

            User user = userService.getUserInfo(userId, userId);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            // 获取钱包信息
            Wallet wallet = walletService.getUserWallet(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getUserId());
            result.put("username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("avatar", user.getAvatar());
            result.put("school", user.getSchool());
            result.put("major", user.getMajor());
            result.put("bio", user.getBio());
            result.put("walletConnected", wallet != null);
            if (wallet != null) {
                result.put("walletAddress", wallet.getWalletAddress());
            }

            // 平台积分余额（Wei → MATIC 字符串）
            if (user.getPlatformBalance() != null) {
                result.put("platformBalance", user.getPlatformBalance().toString());
            } else {
                result.put("platformBalance", "0");
            }

            // 返回用户角色（user / admin / super_admin）
            try {
                String role = permissionClient.getUserRole(userId);
                result.put("role", role != null ? role : "user");
            } catch (Exception e) {
                log.warn("Failed to get user role for userId={}: {}", userId, e.getMessage());
                result.put("role", "user");
            }

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取用户信息失败：error={}", e.getMessage());
            return ApiResponse.error("获取用户信息失败");
        }
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/info")
    public ApiResponse<Map<String, Object>> updateUserInfo(
            @RequestBody Map<String, Object> userData,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtil.parseToken(extractToken(token));
            log.info("更新用户信息：userId={}, data={}", userId, userData);

            User user = userService.getUserInfo(userId, userId);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            // 更新用户信息
            if (userData.containsKey("username")) {
                user.setUsername((String) userData.get("username"));
            }
            if (userData.containsKey("avatar")) {
                user.setAvatar((String) userData.get("avatar"));
            }
            if (userData.containsKey("school")) {
                user.setSchool((String) userData.get("school"));
            }
            if (userData.containsKey("major")) {
                user.setMajor((String) userData.get("major"));
            }
            if (userData.containsKey("bio")) {
                user.setBio((String) userData.get("bio"));
            }

            // 保存更新
            userService.updateById(user);

            // 返回更新后的信息
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getUserId());
            result.put("username", user.getUsername());
            result.put("avatar", user.getAvatar());
            result.put("school", user.getSchool());
            result.put("major", user.getMajor());
            result.put("bio", user.getBio());

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("更新用户信息失败：error={}", e.getMessage());
            return ApiResponse.error("更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录（返回 JWT + 钱包信息）
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestParam String username,
                                     @RequestParam String password,
                                     HttpServletRequest request) {
        log.info("收到登录请求：username={}, IP={}", username, request.getRemoteAddr());
        String token = userService.login(username, password);

        if (token == null) {
            log.warn("登录请求失败：username={}", username);
            return ApiResponse.error("用户名或密码错误");
        }

        Long userId = jwtUtil.parseToken(extractToken(token));
        operationLogProducer.sendOperationLog(userId, "login", request.getRemoteAddr(),
                "用户登录：" + username);
        log.info("登录成功生成 Token: userId={}", userId);

        // 生成 Refresh Token 并存入 Redis
        String refreshToken = jwtUtil.createRefreshToken(userId);
        redisTemplate.opsForValue().set(
            "refresh:" + userId, refreshToken,
            jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);

        // 获取用户钱包信息
        Wallet wallet = walletService.getUserWallet(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("userId", userId);
        result.put("walletConnected", wallet != null);
        if (wallet != null) {
            result.put("walletAddress", wallet.getWalletAddress());
        }
        log.info("登录成功，钱包状态：userId={}, hasWallet={}", userId, wallet != null);

        return ApiResponse.success(result);
    }

    /**
     * 旧版登录接口（仅返回 Token，兼容用）
     */
    @PostMapping("/login/token-only")
    public String loginTokenOnly(@RequestParam String username,
                                 @RequestParam String password,
                                 HttpServletRequest request) {
        log.info("收到登录请求 (Token Only): username={}, IP={}", username, request.getRemoteAddr());
        String token = userService.login(username, password);

        if (token != null) {
            Long userId = jwtUtil.parseToken(extractToken(token));
            operationLogProducer.sendOperationLog(userId, "login", request.getRemoteAddr(),
                    "用户登录：" + username);
            log.info("登录成功生成 Token: userId={}", userId);
        } else {
            log.warn("登录请求失败：username={}", username);
        }
        return token;
    }

    /**
     * 获取或创建用户钱包
     */
    @PostMapping("/wallet/get-or-create")
    public Map<String, Object> getOrCreateWallet(
            @RequestParam Long userId,
            @RequestParam String password,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        log.info("获取或创建钱包请求：userId={}", userId);

        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = jwtUtil.parseToken(extractToken(token));
            if (!currentUserId.equals(userId)) {
                log.warn("无权访问他人钱包：operator={}, target={}", currentUserId, userId);
                result.put("error", "无权访问");
                return result;
            }

            Wallet wallet = walletService.getOrCreateWallet(userId, password);
            result.put("success", true);
            result.put("walletAddress", wallet.getWalletAddress());
            result.put("keystorePath", wallet.getKeystorePath());
            log.info("钱包获取/创建成功：userId={}, address={}", userId, wallet.getWalletAddress());
        } catch (Exception e) {
            log.error("获取/创建钱包失败：userId={}, error={}", userId, e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/users")
    public Page<User> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long currentUserId = jwtUtil.parseToken(extractToken(token));
        log.info("查询用户列表：userId={}, page={}/size={}",
                currentUserId, pageNum, pageSize);

        operationLogProducer.sendOperationLog(currentUserId, "list_users",
                request.getRemoteAddr(), currentUserId + "查询用户列表");

        Page<User> page = userService.getUserList(new Page<>(pageNum, pageSize), currentUserId);
        log.debug("返回用户列表：记录数={}", page.getRecords().size());
        return page;
    }

    @GetMapping("/{userId}")
    public User getUserInfo(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long currentUserId = jwtUtil.parseToken(extractToken(token));
        log.info("查询用户详情：operator={}, target={}", currentUserId, userId);

        operationLogProducer.sendOperationLog(currentUserId, "get_user_info",
                request.getRemoteAddr(), currentUserId + "查询用户 ID=" + userId);

        User user = userService.getUserInfo(userId, currentUserId);
        if (user != null) {
            log.debug("返回用户数据：userId={}", userId);
        } else {
            log.warn("用户详情查询无结果：target={}", userId);
        }
        return user;
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable Long userId,
            @RequestParam String newEmail,
            @RequestParam String newPhone,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long currentUserId = jwtUtil.parseToken(extractToken(token));
        log.info("更新用户请求：operator={}, target={}", currentUserId, userId);

        User oldUser = userService.getUserInfo(userId, currentUserId);

        boolean updateSuccess = userService.updateUserInfo(userId, currentUserId, newEmail, newPhone);

        if (updateSuccess) {
            User newUser = userService.getUserInfo(userId, currentUserId);
            operationLogProducer.sendUpdateLog(
                    currentUserId,
                    userId,
                    oldUser,
                    newUser,
                    request.getRemoteAddr());
            log.info("用户更新成功：target={}", userId);
            return ResponseEntity.ok().build();
        }

        log.warn("用户更新失败：operator={} 无权限修改 target={}", currentUserId, userId);
        return ResponseEntity.status(403).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam Long userId,
            @RequestParam String newPassword,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long currentUserId = jwtUtil.parseToken(extractToken(token));
        log.info("密码重置请求：operator={}, target={}", currentUserId, userId);

        if (userService.resetPassword(userId, newPassword, currentUserId)) {
            operationLogProducer.sendOperationLog(currentUserId, "reset_password",
                    request.getRemoteAddr(), currentUserId + "重置用户 ID[" + userId + "]密码");
            log.info("密码重置成功：target={}", userId);
            return ResponseEntity.ok().build();
        }

        log.warn("密码重置失败：operator={} 无权限操作 target={}", currentUserId, userId);
        return ResponseEntity.status(403).build();
    }

    // 附加角色升级接口
    @PostMapping("/upgrade-to-admin")
    public ResponseEntity<?> upgradeToAdmin(
            @RequestParam Long userId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long currentUserId = jwtUtil.parseToken(extractToken(token));
        log.info("角色升级请求：operator={}, target={}", currentUserId, userId);

        try {
            if(permissionClient.getUserRole(currentUserId).equals("super_admin"))
            {
            permissionClient.upgradeToAdmin(userId);
            operationLogProducer.sendOperationLog(currentUserId, "role_upgrade",
                    request.getRemoteAddr(), currentUserId + "将用户 ID[" + userId + "]升级为管理员");
            log.info("角色升级成功：target={}", userId);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("角色升级失败：target={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // 附加角色降级接口
    @PostMapping("/downgrade-to-user")
    public ResponseEntity<?> downgradeToUser(
            @RequestParam Long userId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long currentUserId = jwtUtil.parseToken(extractToken(token));
        log.info("角色降级请求：operator={}, target={}", currentUserId, userId);

        try {
            if(permissionClient.getUserRole(currentUserId).equals("super_admin"))
            {
                permissionClient.downgradeToUser(userId);
                operationLogProducer.sendOperationLog(currentUserId, "role_downgrade",
                        request.getRemoteAddr(), currentUserId + "将用户 ID[" + userId + "]降级为普通用户");
                log.info("角色降级成功：target={}", userId);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("角色降级失败：target={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 刷新 Access Token
     */
    @PostMapping("/token/refresh")
    public ApiResponse<Map<String, Object>> refreshToken(
            @RequestParam String refreshToken) {
        try {
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return ApiResponse.error("无效的 Refresh Token");
            }
            Long userId = jwtUtil.parseToken(refreshToken);

            // 验证 Redis 中存储的 Refresh Token 是否匹配（防止已注销的 Token 被复用）
            String stored = redisTemplate.opsForValue().get("refresh:" + userId);
            if (!refreshToken.equals(stored)) {
                return ApiResponse.error("Refresh Token 已失效，请重新登录");
            }

            String newAccessToken = jwtUtil.createToken(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("token", newAccessToken);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.warn("Refresh Token 刷新失败：{}", e.getMessage());
            return ApiResponse.error("Refresh Token 无效或已过期");
        }
    }

}