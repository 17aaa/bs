package com.zhm.springboot.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色 Mapper 接口
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}