package com.zhm.springboot.userservice.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}