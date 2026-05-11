package com.zhm.springboot.userservice.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    /**
     * 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        log.info("MyBatis Plus pagination plugin configured");
        return interceptor;
    }

    /**
     * 自动填充字段处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                log.debug("Auto-filling insert fields");
                // 自动填充创建时间和更新时间
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
                // 自动填充逻辑删除字段
                this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
                // 自动填充访问计数
                this.strictInsertFill(metaObject, "accessCount", Integer.class, 0);
                // 自动填充状态
                this.strictInsertFill(metaObject, "status", String.class, "active");
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                log.debug("Auto-filling update fields");
                // 自动填充更新时间
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
