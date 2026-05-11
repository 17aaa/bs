package com.zhm.springboot.userservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhm.springboot.userservice.entity.User;

public interface UserService {
    User register(User user);
    String login(String username, String password);

    Page<User> getUserList(Page<User> page, Long currentUserId);

    User getUserInfo(Long userId, Long currentUserId);

    boolean updateUserInfo(Long userId, Long currentUserId,String newEmail,String newPhone);

    boolean resetPassword(Long userId, String newPassword, Long currentUserId);

    boolean updateById(User user);

}
