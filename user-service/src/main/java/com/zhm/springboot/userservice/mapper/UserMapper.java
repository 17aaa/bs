package com.zhm.springboot.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}